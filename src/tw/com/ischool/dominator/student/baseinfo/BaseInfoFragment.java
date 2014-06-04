package tw.com.ischool.dominator.student.baseinfo;

import ischool.dsa.utility.XmlHelper;
import ischool.dsa.utility.XmlUtil;

import java.util.ArrayList;

import org.w3c.dom.Element;

import tw.com.ischool.dominator.R;
import tw.com.ischool.dominator.student.StudentActivity;
import tw.com.ischool.dominator.util.StringUtil;
import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
//import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
//import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
//import se.emilsjolander.stickylistheaders.StickyListHeadersListView.OnHeaderClickListener;

public class BaseInfoFragment extends Fragment {

	private Activity mActivity;
	private Element mStudentElement;
	private ListView mListView;
	private ArrayList<InfoObject> mInfoList = new ArrayList<InfoObject>();
	private InfoAdapter mAdapter;
	private String mHeaderPhone, mHeaderAddress;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_student_base_info,
				container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mActivity = getActivity();
		mHeaderPhone = mActivity.getString(R.string.base_student_header_phone);
		mHeaderAddress = mActivity
				.getString(R.string.base_student_header_address);

		String xmlString = getArguments().getString(
				StudentActivity.PARAM_STUDENT);
		mStudentElement = XmlHelper.parseXml(xmlString);

		String value = XmlUtil.getElementText(mStudentElement, "StudentName");
		TextView textView = (TextView) mActivity
				.findViewById(R.id.txtStudentName);
		textView.setText(value);

		value = XmlUtil.getElementText(mStudentElement, "StudentNumber");
		textView = (TextView) mActivity.findViewById(R.id.txtStudentNumber);
		textView.setText(value);

		value = XmlUtil.getElementText(mStudentElement, "ClassName");
		textView = (TextView) mActivity.findViewById(R.id.txtClassName);
		textView.setText(value);

		value = XmlUtil.getElementText(mStudentElement, "SeatNo");
		textView = (TextView) mActivity.findViewById(R.id.txtSeatNo);
		textView.setText(value);

		value = XmlUtil.getElementText(mStudentElement, "Gender");
		textView = (TextView) mActivity.findViewById(R.id.txtGender);
		textView.setText(value);

		// value = XmlUtil.getElementText(mStudentElement, "FreshmanPhoto");
		ImageView imgPhoto = (ImageView) mActivity.findViewById(R.id.imgPhoto);
		Bitmap photo = (Bitmap) getArguments().getParcelable(
				StudentActivity.PARAM_PHOTO);
		imgPhoto.setImageBitmap(photo);

		// if (!StringUtil.isNullOrWhitespace(value)) {
		//
		// Bitmap decodedByte = StringUtil.toBitmap(value);
		// imgPhoto.setImageBitmap(decodedByte);
		// } else {
		// imgPhoto.setImageResource(R.drawable.ic_student);
		// }

		mListView = (ListView) mActivity
				.findViewById(R.id.lvInfo);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long itemid) {				
				InfoObject info = mInfoList.get(position);
				if (info.header.equals(mHeaderPhone)) {
					try {
						Intent callIntent = new Intent(Intent.ACTION_DIAL);
						callIntent.setData(Uri.parse("tel:" + info.info));
						startActivity(callIntent);
					} catch (ActivityNotFoundException e) {
						// 表示裝置沒辦法打電話
					}
				} else if (info.header.equals(mHeaderAddress)) {
					// TODO google map
					// Intent mapIntent = new Intent(mActivity,
					// MapActivity.class);
					// mapIntent.putExtra(MapActivity.PARAM_ADDRESS, info.info);
					// startActivity(mapIntent);

					// https://maps.google.com/maps?f=d&saddr=台北火車站&daddr=新竹火車站&hl=tw
					String url = "https://maps.google.com/maps?f=d&saddr=%s&daddr=%s&hl=tw";
					url = String.format(url, StringUtil.EMPTY, info.info);
					Uri uri = Uri.parse(url);
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);
					intent.setData(uri);
					startActivity(intent);
				}
			}
		});

//		mListView.setOnHeaderClickListener(new OnHeaderClickListener() {
//
//			@Override
//			public void onHeaderClick(StickyListHeadersListView l, View header,
//					int itemPosition, long headerId, boolean currentlySticky) {
//
//			}
//		});

		addData();

		mAdapter = new InfoAdapter(mActivity);
		mListView.setAdapter(mAdapter);
	}

	private void addData() {
		mInfoList.clear();

		String headerParent = mActivity
				.getString(R.string.base_student_header_parent);
		addItem(headerParent,
				mActivity.getString(R.string.base_student_father_name),
				"FatherName");
		addItem(headerParent,
				mActivity.getString(R.string.base_student_mother_name),
				"MotherName");
		addItem(headerParent,
				mActivity.getString(R.string.base_student_custodian_name),
				"FatherName");

		addItem(mHeaderPhone,
				mActivity.getString(R.string.base_student_phone_permanent),
				"PermanentPhone");
		addItem(mHeaderPhone,
				mActivity.getString(R.string.base_student_phone_contact),
				"ContactPhone");
		addItem(mHeaderPhone,
				mActivity.getString(R.string.base_student_phone_sms),
				"SMSPhone");
		addXmlItem(mHeaderPhone,
				mActivity.getString(R.string.base_student_phone_other),
				"OtherPhones", "PhoneNumber");

		addAddrItem(mHeaderAddress,
				mActivity.getString(R.string.base_student_address_permanent),
				"PermanentAddress");
		addAddrItem(mHeaderAddress,
				mActivity.getString(R.string.base_student_address_mailing),
				"MailingAddress");
		addAddrItem(mHeaderAddress,
				mActivity.getString(R.string.base_student_address_other),
				"OtherAddresses");
	}

	private void addAddrItem(String header, String subtitle, String elementName) {
		String xmlString = XmlUtil.getElementText(mStudentElement, elementName);
		if (!StringUtil.isNullOrWhitespace(xmlString)) {
			Element phoneElement = XmlHelper.parseXml(xmlString);
			int i = 0;
			for (Element e : XmlUtil.selectElements(phoneElement, "Address")) {

				// String infoString = e.getTextContent();

				String county = XmlUtil.getElementText(e, "County");
				String town = XmlUtil.getElementText(e, "Town");
				String district = XmlUtil.getElementText(e, "District");
				String area = XmlUtil.getElementText(e, "Area");
				String detailAddress = XmlUtil.getElementText(e,
						"DetailAddress");

				String address = county + town + district + area
						+ detailAddress;

				addItemWithString(header, subtitle, i, address);
				i++;
			}
		}
	}

	private void addXmlItem(String header, String subtitle, String elementName,
			String subElementName) {
		String xmlString = XmlUtil.getElementText(mStudentElement, elementName);
		if (!StringUtil.isNullOrWhitespace(xmlString)) {
			Element phoneElement = XmlHelper.parseXml(xmlString);
			int i = 0;
			for (Element e : XmlUtil.selectElements(phoneElement,
					subElementName)) {
				i++;
				String infoString = e.getTextContent();
				addItemWithString(header, subtitle, i, infoString);
			}
		}
	}

	private void addItem(String header, String subtitle, String elementName) {
		String infoString = XmlUtil
				.getElementText(mStudentElement, elementName);
		addItemWithString(header, subtitle, 0, infoString);
	}

	private void addItemWithString(String header, String subtitle,
			int subtitleIndex, String infoString) {
		if (StringUtil.isNullOrWhitespace(infoString))
			return;

		while (subtitle.length() < 4) {
			subtitle = "　" + subtitle;
		}

		String indexString = " ";
		if (subtitleIndex > 0) {
			indexString = String.valueOf(subtitleIndex);
		}

		InfoObject info = new InfoObject();
		info.header = header;
		info.subtitle = subtitle;
		info.info = infoString;
		info.subtitleIndex = indexString;
		mInfoList.add(info);
	}

	private class InfoAdapter extends BaseAdapter implements
			 SectionIndexer {

		private int[] mSectionIndices;
		private String[] mSectionLetters;
		private LayoutInflater mInflater;
//		private MessageDigest mMD5;

		public InfoAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
			mSectionIndices = getSectionIndices();
			mSectionLetters = getSectionLetters();
		}

		@Override
		public int getCount() {
			return mInfoList.size();
		}

		@Override
		public Object getItem(int position) {
			return mInfoList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ContentViewHolder holder;

			if (convertView == null) {
				holder = new ContentViewHolder();
				convertView = mInflater.inflate(R.layout.item_info_content,
						parent, false);
				holder.txtSubtitle = (TextView) convertView
						.findViewById(R.id.txtSubtitle);
				holder.txtContent = (TextView) convertView
						.findViewById(R.id.txtInfo);
				holder.txtSubtitleIndex = (TextView) convertView
						.findViewById(R.id.txtSubtitleIndex);
				convertView.setTag(holder);
			} else {
				holder = (ContentViewHolder) convertView.getTag();
			}

			InfoObject info = mInfoList.get(position);
			holder.txtSubtitle.setText(info.subtitle);
			holder.txtContent.setText(info.info);
			holder.txtSubtitleIndex.setText(info.subtitleIndex);

			return convertView;
		}

		@Override
		public int getPositionForSection(int section) {
			if (section >= mSectionIndices.length) {
				section = mSectionIndices.length - 1;
			} else if (section < 0) {
				section = 0;
			}
			return mSectionIndices[section];
		}

		@Override
		public int getSectionForPosition(int position) {
			for (int i = 0; i < mSectionIndices.length; i++) {
				if (position < mSectionIndices[i]) {
					return i - 1;
				}
			}
			return mSectionIndices.length - 1;
		}

		@Override
		public Object[] getSections() {
			return mSectionLetters;
		}

//		@Override
//		public View getHeaderView(int position, View convertView,
//				ViewGroup parent) {
//			HeaderViewHolder holder;
//
//			if (convertView == null) {
//				holder = new HeaderViewHolder();
//				convertView = mInflater.inflate(R.layout.item_info_header,
//						parent, false);
//				holder.textView = (TextView) convertView
//						.findViewById(R.id.txtHeader);
//				convertView.setTag(holder);
//			} else {
//				holder = (HeaderViewHolder) convertView.getTag();
//			}
//
//			holder.textView.setText(mInfoList.get(position).header);
//
//			return convertView;
//		}

//		@Override
//		public long getHeaderId(int position) {
//			InfoObject info = mInfoList.get(position);
//			return computeString(info.header);
//		}

		@Override
		public boolean isEnabled(int position) {
			InfoObject info = mInfoList.get(position);
			if (info.header.equals(mHeaderAddress)
					|| info.header.equals(mHeaderPhone))
				return true;
			return false;
		}

//		private long computeString(String plaintext) {
//			long hash = plaintext.hashCode();
//
//			try {
//				if (mMD5 == null) {
//					mMD5 = MessageDigest.getInstance("MD5");
//				}
//
//				mMD5.reset();
//				mMD5.update(plaintext.getBytes());
//				byte[] digest = mMD5.digest();
//				BigInteger bigInt = new BigInteger(1, digest);
//				hash = bigInt.longValue();
//			} catch (NoSuchAlgorithmException e) {
//
//			}
//			return hash;
//		}

		private int[] getSectionIndices() {
			ArrayList<Integer> sectionIndices = new ArrayList<Integer>();
			if (mInfoList != null && mInfoList.size() > 0) {
				// JSONObject json = mJSONObjects.get(0);
				// JSONObject knowObject = JSONUtil.getJSONObject(json, "know");
				String lastHeader = mInfoList.get(0).header;

				// char lastFirstChar = JSONUtil.getString(json, ""); //
				// mCountries[0].charAt(0);
				sectionIndices.add(0);
				for (int i = 1; i < mInfoList.size(); i++) {
					// json = mJSONObjects.get(i);
					// knowObject = JSONUtil.getJSONObject(json, "know");
					String header = mInfoList.get(i).header;

					if (!lastHeader.equals(header)) {
						lastHeader = header;
						sectionIndices.add(i);
					}
					// if (mCountries[i].charAt(0) != lastFirstChar) {
					// lastFirstChar = mCountries[i].charAt(0);
					// sectionIndices.add(i);
					// }
				}
			}
			int[] sections = new int[sectionIndices.size()];
			for (int i = 0; i < sectionIndices.size(); i++) {
				sections[i] = sectionIndices.get(i);
			}
			return sections;
		}

		private String[] getSectionLetters() {
			String[] letters = new String[mSectionIndices.length];
			for (int i = 0; i < mSectionIndices.length; i++) {
				// JSONObject json = mJSONObjects.get(i);
				// JSONObject knowObject = JSONUtil.getJSONObject(json, "know");
				// letters[i] = JSONUtil.getString(knowObject, "name");
				letters[i] = mInfoList.get(i).header;
			}
			return letters;
		}
	}

	class InfoObject {
		String subtitleIndex;
		String header;
		String subtitle;
		String info;
	}

	class HeaderViewHolder {
		TextView textView;
	}

	class ContentViewHolder {
		TextView txtSubtitle;
		TextView txtSubtitleIndex;
		TextView txtContent;
	}
}
