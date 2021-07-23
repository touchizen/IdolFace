package com.touchizen.idolface.ui.logout

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.touchizen.idolface.MainActivity
import com.touchizen.idolface.databinding.AlertLogoutBinding
import com.touchizen.idolface.model.UserStatus
import com.touchizen.idolface.utils.MPreference
import org.greenrobot.eventbus.EventBus


class LogoutFragment : DialogFragment() {

    private lateinit var binding: AlertLogoutBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = context?.let { AlertDialog.Builder(it) }
        val binding: AlertLogoutBinding = AlertLogoutBinding.inflate(layoutInflater)
        builder!!.setView( binding.root)

//        dialog!!.window!!.setLayout(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )

        binding.txtOk.setOnClickListener {
            onOk()
        }
        binding.txtCancel.setOnClickListener {
            onCancel()
        }

        return builder.create()
    }

    fun onOk() {
        val preference = MPreference(requireActivity().getBaseContext());
        dialog?.dismiss();
        EventBus.getDefault().post(UserStatus("offline"));
        FirebaseAuth.getInstance().signOut();
        preference.clearAll();
        (requireActivity() as MainActivity).onLogoutFinished()
    }

    fun onCancel() {
        dialog?.dismiss();
    }
}