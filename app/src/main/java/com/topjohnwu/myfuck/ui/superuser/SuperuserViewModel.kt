package com.topjohnwu.myfuck.ui.superuser

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.viewModelScope
import com.topjohnwu.myfuck.BR
import com.topjohnwu.myfuck.R
import com.topjohnwu.myfuck.arch.BaseViewModel
import com.topjohnwu.myfuck.arch.adapterOf
import com.topjohnwu.myfuck.arch.diffListOf
import com.topjohnwu.myfuck.arch.itemBindingOf
import com.topjohnwu.myfuck.core.Config
import com.topjohnwu.myfuck.core.myfuckdb.PolicyDao
import com.topjohnwu.myfuck.core.model.su.SuPolicy
import com.topjohnwu.myfuck.core.utils.BiometricHelper
import com.topjohnwu.myfuck.core.utils.currentLocale
import com.topjohnwu.myfuck.databinding.ComparableRvItem
import com.topjohnwu.myfuck.events.SnackbarEvent
import com.topjohnwu.myfuck.events.dialog.BiometricEvent
import com.topjohnwu.myfuck.events.dialog.SuperuserRevokeDialog
import com.topjohnwu.myfuck.utils.Utils
import com.topjohnwu.myfuck.utils.asText
import com.topjohnwu.myfuck.view.TappableHeadlineItem
import com.topjohnwu.myfuck.view.TextItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.bindingcollectionadapter2.collections.MergeObservableList

class SuperuserViewModel(
    private val db: PolicyDao
) : BaseViewModel(), TappableHeadlineItem.Listener {

    private val itemNoData = TextItem(R.string.superuser_policy_none)

    private val itemsPolicies = diffListOf<PolicyRvItem>()
    private val itemsHelpers = ObservableArrayList<TextItem>()

    val adapter = adapterOf<ComparableRvItem<*>>()
    val items = MergeObservableList<ComparableRvItem<*>>().apply {
        if (Config.myfuckHide)
            insertItem(TappableHeadlineItem.Hide)
    }.insertList(itemsHelpers)
        .insertList(itemsPolicies)
    val itemBinding = itemBindingOf<ComparableRvItem<*>> {
        it.bindExtra(BR.listener, this)
    }

    // ---

    override fun refresh() = viewModelScope.launch {
        if (!Utils.showSuperUser()) {
            state = State.LOADING_FAILED
            return@launch
        }
        state = State.LOADING
        val (policies, diff) = withContext(Dispatchers.Default) {
            db.deleteOutdated()
            val policies = db.fetchAll {
                PolicyRvItem(it, it.icon, this@SuperuserViewModel)
            }.sortedWith(compareBy(
                { it.item.appName.toLowerCase(currentLocale) },
                { it.item.packageName }
            ))
            policies to itemsPolicies.calculateDiff(policies)
        }
        itemsPolicies.update(policies, diff)
        if (itemsPolicies.isNotEmpty())
            itemsHelpers.clear()
        else if (itemsHelpers.isEmpty())
            itemsHelpers.add(itemNoData)
        state = State.LOADED
    }

    // ---

    override fun onItemPressed(item: TappableHeadlineItem) = when (item) {
        TappableHeadlineItem.Hide -> hidePressed()
        else -> Unit
    }

    private fun hidePressed() =
        SuperuserFragmentDirections.actionSuperuserFragmentToHideFragment().navigate()

    fun deletePressed(item: PolicyRvItem) {
        fun updateState() = viewModelScope.launch {
            db.delete(item.item.uid)
            itemsPolicies.removeAll { it.genericItemSameAs(item) }
            if (itemsPolicies.isEmpty() && itemsHelpers.isEmpty()) {
                itemsHelpers.add(itemNoData)
            }
        }

        if (BiometricHelper.isEnabled) {
            BiometricEvent {
                onSuccess { updateState() }
            }.publish()
        } else {
            SuperuserRevokeDialog {
                appName = item.item.appName
                onSuccess { updateState() }
            }.publish()
        }
    }

    //---

    fun updatePolicy(policy: SuPolicy, isLogging: Boolean) = viewModelScope.launch {
        db.update(policy)
        val res = when {
            isLogging -> when {
                policy.logging -> R.string.su_snack_log_on
                else -> R.string.su_snack_log_off
            }
            else -> when {
                policy.notification -> R.string.su_snack_notif_on
                else -> R.string.su_snack_notif_off
            }
        }
        SnackbarEvent(res.asText(policy.appName)).publish()
    }

    fun togglePolicy(item: PolicyRvItem, enable: Boolean) {
        fun updateState() {
            item.policyState = enable

            val policy = if (enable) SuPolicy.ALLOW else SuPolicy.DENY
            val app = item.item.copy(policy = policy)

            viewModelScope.launch {
                db.update(app)
                val res = if (app.policy == SuPolicy.ALLOW) R.string.su_snack_grant
                else R.string.su_snack_deny
                SnackbarEvent(res.asText(item.item.appName)).publish()
            }
        }

        if (BiometricHelper.isEnabled) {
            BiometricEvent {
                onSuccess { updateState() }
            }.publish()
        } else {
            updateState()
        }
    }
}
