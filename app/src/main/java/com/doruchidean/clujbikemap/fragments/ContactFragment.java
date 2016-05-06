package com.doruchidean.clujbikemap.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.doruchidean.clujbikemap.R;
import com.doruchidean.clujbikemap.helpers.CBMProgressDialog;

/**
 * Created by Doru on 03/05/16.
 *
 */
public class ContactFragment extends android.support.v4.app.Fragment {

	@Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View fragmentView = inflater.inflate(R.layout.fragment_contact, container, false);

		TextView btnContactDev = (TextView) fragmentView.findViewById(R.id.btn_contact_developer);
		TextView btnContactCallCenter = (TextView) fragmentView.findViewById(R.id.btn_contact_call_center);
		TextView btnCommunitySupport = (TextView) fragmentView.findViewById(R.id.btn_community_support);

		btnContactDev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
				emailIntent.setData(Uri.parse("mailto:"));
				emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getContext().getString(R.string.developer_email)});
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "ClujBike Map - android support");
				emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello, \n\n");
				getContext().startActivity(Intent.createChooser(emailIntent, "Send email..."));
			}
		});
		btnContactCallCenter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent callIntent = new Intent(Intent.ACTION_DIAL);
				callIntent.setData(Uri.parse("tel:0371784172"));
				getContext().startActivity(callIntent);
			}
		});
		btnCommunitySupport.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse(getContext().getString(R.string.community_page_web_url));
				try {
					ApplicationInfo applicationInfo = getContext().getPackageManager().getApplicationInfo("com.facebook.katana", 0);
					if (applicationInfo.enabled) {
						// http://stackoverflow.com/a/24547437/1048340
						uri = Uri.parse("fb://facewebmodal/f?href=" + getContext().getString(R.string.community_page_web_url));
					}
				} catch (PackageManager.NameNotFoundException e) {
					e.printStackTrace();
				}

				getContext().startActivity(
					new Intent(Intent.ACTION_VIEW, uri)
				);

				CBMProgressDialog progressDialog = new CBMProgressDialog(getContext());
				progressDialog.setIndeterminate(true);
				progressDialog.setMessage("Redirecting...");
				progressDialog.setCancelable(true);
				progressDialog.show();
				progressDialog.setMaxTimeLimitSeconds(3);
			}
		});

		return fragmentView;
	}

}
