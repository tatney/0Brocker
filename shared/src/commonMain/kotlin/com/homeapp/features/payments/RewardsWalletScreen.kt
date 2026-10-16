package com.homeapp.features.payments
import androidx.compose.runtime.Composable
@Composable fun RewardsWalletScreen(onBack: ()->Unit,onAddMoney: ()->Unit,onTransactions: ()->Unit,onSendMoney: ()->Unit={},onPayments: ()->Unit={},onProperty: ()->Unit={},onServices: ()->Unit={},onChat: ()->Unit={},onProfile: ()->Unit={},onSaved: ()->Unit={}) {
 PaymentsStorefrontScreen(walletMode=true,onProperty=onProperty,onServices=onServices,onChat=onChat,onProfile=onProfile,onSaved=onSaved,onHome=onBack,onAddMoney=onAddMoney,onSendMoney=onSendMoney,onViewAll=onTransactions,onPaymentsPage=onPayments)
}
