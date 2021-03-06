/* -*- Mode:jde; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */
/*
 * Copyright (c) 2015-2019 Regents of the University of California
 * <p/>
 * This file is part of NFD (Named Data Networking Forwarding Daemon) Android.
 * See AUTHORS.md for complete list of NFD Android authors and contributors.
 * <p/>
 * NFD Android is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * NFD Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License along with
 * NFD Android, e.g., in COPYING.md file.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.named_data.nfd;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.intel.jndn.management.enums.FacePersistency;
import com.intel.jndn.management.enums.FaceScope;
import com.intel.jndn.management.enums.LinkType;
import com.intel.jndn.management.types.FaceStatus;

import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.util.Blob;
import net.named_data.nfd.utils.G;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.ListFragment;

public class FaceStatusFragment extends ListFragment {

  /**
   * Create a new instance of {@link net.named_data.nfd.FaceStatusFragment} with
   * the given detail face information as a byte array object.
   * 
   * Note:
   * faceStatus.wireEncode().getImmutableArray()
   * This byte array should be retrieved via a call to:
   * {@link com.intel.jndn.management.types.FaceStatus#wireEncode()} for a
   * {@link net.named_data.jndn.util.Blob} object. Subsequently, an
   * immutable byte array can be retrieved via
   * {@link net.named_data.jndn.util.Blob#getImmutableArray()}.
   * 
   * @param faceStatus FaceStatus instance with information about the face
   * @return Fragment instance of {@link net.named_data.nfd.FaceStatusFragment}
   * that is ready for use by the hosting activity
   */
  public static FaceStatusFragment
  newInstance(FaceStatus faceStatus) {
    Bundle args = new Bundle();
    args.putByteArray(EXTRA_FACE_INFORMATION, faceStatus.wireEncode().getImmutableArray());

    FaceStatusFragment fragment = new FaceStatusFragment();
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);

    View v = getLayoutInflater().inflate(R.layout.fragment_face_detail_list_header, getListView(), false);
    getListView().addHeaderView(v, null, false);
    getListView().setDivider(ContextCompat.getDrawable(getContext(), R.drawable.list_item_divider));
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState)
  {
    super.onActivityCreated(savedInstanceState);

    if (m_faceStatusAdapter == null) {
      // List items to be displayed; Used when creating
      // {@link net.named_data.nfd.FaceStatusFragment.FaceStatusAdapter}
      ArrayList<ListItem> listItems = new ArrayList<>();

      Resources res = getResources();
      m_scopes = res.getStringArray(R.array.face_scopes);
      m_linkTypes = res.getStringArray(R.array.face_link_types);
      m_persistencies = res.getStringArray(R.array.face_persistency);

      // Get face status information
      FaceStatus faceStatus = new FaceStatus();
      try {
        byte[] args = getArguments().getByteArray(EXTRA_FACE_INFORMATION);
        if (args == null) {
          throw new EncodingException("Not extra face in formation available");
        }
        faceStatus.wireDecode(new Blob(args).buf());
      } catch (EncodingException e) {
        G.Log("EXTRA_FACE_INFORMATION: EncodingException: " + e);
      }

      // Creating list of items to be displayed
      listItems.add(new ListItem(R.string.face_id, String.valueOf(faceStatus.getFaceId())));
      listItems.add(new ListItem(R.string.local_face_uri, faceStatus.getLocalUri()));
      listItems.add(new ListItem(R.string.remote_face_uri, faceStatus.getRemoteUri()));
      listItems.add(new ListItem(R.string.expires_in, faceStatus.getExpirationPeriod() < 0 ?
        getString(R.string.expire_never) :
        PeriodFormat.getDefault().print(new Period(faceStatus.getExpirationPeriod()))));
      listItems.add(new ListItem(R.string.face_scope, getScope(faceStatus.getFaceScope())));
      listItems.add(new ListItem(R.string.face_persistency, getPersistency(faceStatus.getFacePersistency())));
      listItems.add(new ListItem(R.string.link_type, getLinkType(faceStatus.getLinkType())));
      listItems.add(new ListItem(R.string.in_interests, String.valueOf(
        faceStatus.getNInInterests())));
      listItems.add(new ListItem(R.string.in_data, String.valueOf(faceStatus.getNInData())));
      listItems.add(new ListItem(R.string.out_interests, String.valueOf(
        faceStatus.getNOutInterests())));
      listItems.add(new ListItem(R.string.out_data, String.valueOf(faceStatus.getNOutData())));
      listItems.add(new ListItem(R.string.in_bytes, String.valueOf(faceStatus.getNInBytes())));
      listItems.add(new ListItem(R.string.out_bytes, String.valueOf(faceStatus.getNOutBytes())));

      m_faceStatusAdapter = new FaceStatusAdapter(getActivity(), listItems);
    }
    // setListAdapter must be called after addHeaderView.  Otherwise, there is an exception on some platforms.
    // http://stackoverflow.com/a/8141537/2150331
    setListAdapter(m_faceStatusAdapter);
  }

  @Override
  public void onDestroyView()
  {
    super.onDestroyView();
    setListAdapter(null);
  }
  /////////////////////////////////////////////////////////////////////////

  private String
  getScope(FaceScope scope)
  {
    return m_scopes[scope.toInteger()];
  }

  private String
  getPersistency(FacePersistency persistency)
  {
    return m_persistencies[persistency.toInteger()];
  }

  private String
  getLinkType(LinkType linkType)
  {
    return m_linkTypes[linkType.toInteger()];
  }

  /**
   * Generic list item model for use in
   * {@link net.named_data.nfd.FaceStatusFragment.FaceStatusAdapter}.
   */
  private static class ListItem {
    ListItem(int title, String value) {
      m_title = title;
      m_value = value;
    }

    public String getValue() {
      return m_value;
    }

    public void setValue(String value) {
      m_value = value;
    }

    public int getTitle() {
      return m_title;
    }

    public void setTitle(int title) {
      m_title = title;
    }

    private int m_title;
    private String m_value;
  }

  /**
   * Custom ListView adapter that displays face status information.
   */
  private static class FaceStatusAdapter extends ArrayAdapter<ListItem> {
    private FaceStatusAdapter(Context context, List<ListItem> objects) {
      super(context, 0, objects);
      m_layoutInflater = LayoutInflater.from(context);
    }

    @Override @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
      ListItemHolder holder;
      if (convertView == null) {
        holder = new ListItemHolder();

        convertView = m_layoutInflater.inflate(R.layout.list_item_face_generic_item, parent, false);
        convertView.setTag(holder);

        holder.m_title = convertView.findViewById(R.id.list_item_generic_title);
        holder.m_value = convertView.findViewById(R.id.list_item_generic_value);
      } else {
        holder = (ListItemHolder)convertView.getTag();
      }

      ListItem info = getItem(position);
      if (info != null) {
        holder.m_title.setText(info.getTitle());
        holder.m_value.setText(info.getValue());
      }

      return convertView;
    }

    private static class ListItemHolder {
      private TextView m_title;
      private TextView m_value;
    }

    private final LayoutInflater m_layoutInflater;
  }

  /////////////////////////////////////////////////////////////////////////

  /** Bundle argument key for face information byte array */
  private static final String EXTRA_FACE_INFORMATION
      = "net.named_data.nfd.face_detail_fragment_face_id";

  /** Reference to custom {@link net.named_data.nfd.FaceStatusFragment.FaceStatusAdapter} */
  private FaceStatusAdapter m_faceStatusAdapter;

  private String[] m_scopes;
  private String[] m_persistencies;
  private String[] m_linkTypes;
}
