package com.touchizen.idolface.ui.datepicker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.touchizen.idolface.ui.idolprofile.IdolProfileViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DatePickerFragment extends DialogFragment
		implements DatePickerDialog.OnDateSetListener
{
	IdolProfileViewModel viewModel;
	Button btnDate;
	int year = 0;
	int month = 0;
	int day = 0;

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

		final Calendar c = Calendar.getInstance();
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);

		if (btnDate != null) {
			convertStringToDate(btnDate.getText().toString());
		}

		return new DatePickerDialog(getActivity(),
					android.R.style.Theme_Holo_Dialog,
					this,
					year,month,day
				);
	}

	public void setPickButton(Button btnDate) {
		this.btnDate = btnDate;
	}

	public void setBinding(IdolProfileViewModel viewModel) {
		this.viewModel = viewModel;
	}

	@Override
	public void onDateSet(DatePicker view, int year, int month, int day) {
		//month++;
		String strDate = String.format("%04d/%02d/%02d", year, month+1, day);
		if (viewModel != null) {
			viewModel.getBirthDate().setValue(strDate);
		}
		if (btnDate != null) {
			btnDate.setText(strDate);
		}
	}

	public boolean convertStringToDate(String inDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		dateFormat.setLenient(false);
		try {
			dateFormat.parse(inDate.trim());
			final Calendar tc = dateFormat.getCalendar();
			year = tc.get(Calendar.YEAR);
			month = tc.get(Calendar.MONTH);
			day = tc.get(Calendar.DAY_OF_MONTH);
		} catch (ParseException pe) {
			return false;
		}
		return true;
	}

}