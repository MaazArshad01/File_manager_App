package com.jksol.filemanager.Fragments.NetworkFragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jksol.filemanager.Fragments.GalleryFragment.AllFileTypeFragment;
import com.jksol.filemanager.Fragments.NetworkFragment.Lan.Computer;
import com.jksol.filemanager.Fragments.NetworkFragment.Lan.LanListAdapter;
import com.jksol.filemanager.Fragments.NetworkFragment.Lan.SubnetScanner;
import com.jksol.filemanager.R;
import com.jksol.filemanager.Utils.AppController;
import com.jksol.filemanager.Utils.Futils;
import com.jksol.filemanager.Utils.RecyclerTouchListener;
import com.jksol.filemanager.Utils.Utils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import jcifs.smb.SmbFile;

/**
 * Created by Umiya Mataji on 1/24/2017.
 */

public class LanComputers extends Fragment {

    Context context;
    String path = "", name = "";
    // ======================= Start Local Lan list Variable =================================
    private ArrayList<Computer> computers = new ArrayList<>();
    private SubnetScanner subnetScanner;
    private RecyclerView recycler_view_lan_list;
    private LinearLayout noLanLayout;
    private LanListAdapter lanListAdapter;
    // ======================= End Local Lan list Variable =================================
    private ProgressBar lan_loader;
    // ======================= Start SMB COnnection Variable =================================
    private String emptyAddress, emptyName, invalidDomain, invalidUsername;
    // ======================= END SMB COnnection Variable =================================
    private SmbConnectionListener smbConnectionListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lan_layout, container, false);
        context = getActivity();
        setHasOptionsMenu(true);
        init(view);
        getAllLocalNetworkPC();

        return view;
    }

    public void init(View view) {

        lan_loader = (ProgressBar) view.findViewById(R.id.lan_loader);
        recycler_view_lan_list = (RecyclerView) view.findViewById(R.id.recycler_view_lan_list);
        noLanLayout = (LinearLayout) view.findViewById(R.id.noLanLayout);

        lanListAdapter = new LanListAdapter(computers);
        recycler_view_lan_list.setHasFixedSize(true);

        RecyclerView.LayoutManager mAudioLayoutManager = new LinearLayoutManager(context);
        recycler_view_lan_list.setLayoutManager(mAudioLayoutManager);
        recycler_view_lan_list.setItemAnimator(new DefaultItemAnimator());

        recycler_view_lan_list.setAdapter(lanListAdapter);
        recycler_view_lan_list.addOnItemTouchListener(new RecyclerTouchListener(AppController.getInstance().getApplicationContext(), recycler_view_lan_list, new AllFileTypeFragment.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                // Toast.makeText(context, computers.get(position).name, Toast.LENGTH_SHORT).show();
                SMBConnectDialog(view, position, false);
            }

            @Override
            public void onLongClick(View view, int position) {

            }

        }));
    }

    public void SMBConnectDialog(View view, int position, boolean edit) {

        final Dialog SMBConnect_dialog = new Dialog(context);
        SMBConnect_dialog.setCancelable(true);
        SMBConnect_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        SMBConnect_dialog.setContentView(R.layout.smb_connect_dialog);
        SMBConnect_dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        SMBConnect_dialog.getWindow().getAttributes().windowAnimations = R.style.confirmDeleteAnimation;
        SMBConnect_dialog.show();


        try {
            path = computers.get(position).addr;
            name = computers.get(position).name;
        } catch (Exception e) {
            SMBConnect_dialog.dismiss();
            Toast.makeText(context, new Futils().getString(context, R.string.soemthingwrong), Toast.LENGTH_SHORT).show();
        }
        emptyAddress = String.format(getString(R.string.cantbeempty), getString(R.string.ip));
        emptyName = String.format(getString(R.string.cantbeempty), getString(R.string.connectionname));
        invalidDomain = String.format(getString(R.string.invalid), getString(R.string.domain));
        invalidUsername = String.format(getString(R.string.invalid), getString(R.string.username).toLowerCase());

        if (getActivity() instanceof SmbConnectionListener) {
            smbConnectionListener = (SmbConnectionListener) getActivity();
        }

        final TextInputLayout connectionTIL = (TextInputLayout) SMBConnect_dialog.findViewById(R.id.connectionTIL);
        final TextInputLayout ipTIL = (TextInputLayout) SMBConnect_dialog.findViewById(R.id.ipTIL);
        final TextInputLayout domainTIL = (TextInputLayout) SMBConnect_dialog.findViewById(R.id.domainTIL);
        final TextInputLayout usernameTIL = (TextInputLayout) SMBConnect_dialog.findViewById(R.id.usernameTIL);
        final AppCompatEditText con_name = (AppCompatEditText) SMBConnect_dialog.findViewById(R.id.connectionET);

        con_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (con_name.getText().toString().length() == 0)
                    connectionTIL.setError(emptyName);
                else connectionTIL.setError("");
            }
        });

        final AppCompatEditText ip = (AppCompatEditText) SMBConnect_dialog.findViewById(R.id.ipET);
        ip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (ip.getText().toString().length() == 0)
                    ipTIL.setError(emptyAddress);
                else ipTIL.setError("");
            }
        });

        final AppCompatEditText domain = (AppCompatEditText) SMBConnect_dialog.findViewById(R.id.domainET);
        domain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (domain.getText().toString().contains(";"))
                    domainTIL.setError(invalidDomain);
                else domainTIL.setError("");
            }
        });

        final AppCompatEditText user = (AppCompatEditText) SMBConnect_dialog.findViewById(R.id.usernameET);
        user.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (user.getText().toString().contains(":"))
                    usernameTIL.setError(invalidUsername);
                else usernameTIL.setError("");
            }
        });


        final AppCompatEditText pass = (AppCompatEditText) SMBConnect_dialog.findViewById(R.id.passwordET);
        final AppCompatCheckBox ch = (AppCompatCheckBox) SMBConnect_dialog.findViewById(R.id.checkBox2);
        TextView help = (TextView) SMBConnect_dialog.findViewById(R.id.wanthelp);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Utils().showSMBHelpDialog(context);
            }
        });
        ch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ch.isChecked()) {
                    user.setEnabled(false);
                    pass.setEnabled(false);
                } else {
                    user.setEnabled(true);
                    pass.setEnabled(true);

                }
            }
        });

        if (edit) {
            String userp = "", passp = "", ipp = "", domainp = "";
            con_name.setText(name);
            try {
                jcifs.Config.registerSmbURLHandler();
                URL a = new URL(path);
                String userinfo = a.getUserInfo();
                if (userinfo != null) {
                    String inf = URLDecoder.decode(userinfo, "UTF-8");
                    int domainDelim = !inf.contains(";") ? 0 : inf.indexOf(';');
                    domainp = inf.substring(0, domainDelim);
                    if (domainp != null && domainp.length() > 0)
                        inf = inf.substring(domainDelim + 1);
                    userp = inf.substring(0, inf.indexOf(":"));
                    passp = inf.substring(inf.indexOf(":") + 1, inf.length());
                    domain.setText(domainp);
                    user.setText(userp);
                    pass.setText(passp);
                } else ch.setChecked(true);
                ipp = a.getHost();
                ip.setText(ipp);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        } else if (path != null && path.length() > 0) {
            con_name.setText(name);
            ip.setText(path);
            user.requestFocus();
        } else {
            con_name.setText(R.string.smb_con);
            con_name.requestFocus();
        }

        final Button dialaog_cancel = (Button) SMBConnect_dialog.findViewById(R.id.dialaog_cancel);
        dialaog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (smbConnectionListener != null) {
                    smbConnectionListener.deleteConnection(name, path);
                    SMBConnect_dialog.dismiss();
                }
                SMBConnect_dialog.dismiss();
            }
        });

        final Button dialog_connect = (Button) SMBConnect_dialog.findViewById(R.id.dialog_connect);
        dialog_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s[];
                String ipa = ip.getText().toString();
                String con_nam = con_name.getText().toString();
                String sDomain = domain.getText().toString();
                String username = user.getText().toString();
                TextInputLayout firstInvalidField = null;

                if (con_nam == null || con_nam.length() == 0) {
                    connectionTIL.setError(emptyName);
                    firstInvalidField = connectionTIL;
                }
                if (ipa == null || ipa.length() == 0) {
                    ipTIL.setError(emptyAddress);
                    if (firstInvalidField == null)
                        firstInvalidField = ipTIL;
                }
                if (sDomain.contains(";")) {
                    domainTIL.setError(invalidDomain);
                    if (firstInvalidField == null)
                        firstInvalidField = domainTIL;
                }
                if (username.contains(":")) {
                    usernameTIL.setError(invalidUsername);
                    if (firstInvalidField == null)
                        firstInvalidField = usernameTIL;
                }
                if (firstInvalidField != null) {
                    firstInvalidField.requestFocus();
                    return;
                }

                SmbFile smbFile;
                String domaind = domain.getText().toString();
                if (ch.isChecked()) {
                    smbFile = connectingWithSmbServer(new String[]{ipa, "", "", domaind}, true);
                    Toast.makeText(context, "File :- " + smbFile.getPath(), Toast.LENGTH_SHORT).show();
                } else {
                    String useru = user.getText().toString();
                    String passp = pass.getText().toString();
                    smbFile = connectingWithSmbServer(new String[]{ipa, useru, passp, domaind}, false);
                    Toast.makeText(context, "File :- " + smbFile.getPath(), Toast.LENGTH_SHORT).show();
                }
                if (smbFile == null) return;
                s = new String[]{con_name.getText().toString(), smbFile.getPath()};
                if (smbConnectionListener != null) {
                    smbConnectionListener.addConnection(false, s[0], s[1], name, path);
                }

                SMBConnect_dialog.dismiss();
            }
        });
    }

    public void getAllLocalNetworkPC() {
        computers.clear();
        lanListAdapter.notifyDataSetChanged();

        computers.add(new Computer("-1", "-1"));
        subnetScanner = new SubnetScanner(getActivity());

        subnetScanner.setObserver(new SubnetScanner.ScanObserver() {
            @Override
            public void computerFound(final Computer computer) {
                if (getActivity() != null)
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!computers.contains(computer)) {
                                computers.add(computers.size() - 1, computer);
                                lanListAdapter.notifyDataSetChanged();
                                // Log.d("Local networks", "Name :- " + computer.name + " IP :- " + computer.addr);
                            }
                        }
                    });
            }

            @Override
            public void searchFinished() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //  Log.d("Local networks", "Finish");

                            if (computers.size() == 1) {
                                lan_loader.setVisibility(View.GONE);
                                recycler_view_lan_list.setVisibility(View.GONE);
                                noLanLayout.setVisibility(View.VISIBLE);
                                Toast.makeText(getActivity(), "No device Found.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            computers.remove(computers.size() - 1);
                            lanListAdapter.notifyDataSetChanged();
                            lan_loader.setVisibility(View.GONE);

                        }
                    });
                }
            }
        });
        subnetScanner.start();
    }

    public SmbFile connectingWithSmbServer(String[] auth, boolean anonym) {
        try {
            String yourPeerIP = auth[0], domain = auth[3];
            String path = "smb://" + (android.text.TextUtils.isEmpty(domain) ? "" : (URLEncoder.encode(domain + ";", "UTF-8"))) + (anonym ? "" : (URLEncoder.encode(auth[1] + ":" + auth[2], "UTF-8") + "@")) + yourPeerIP + "/";
            SmbFile smbFile = new SmbFile(path);
            return smbFile;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error :- " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.lan_computer_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_refresh:
                getAllLocalNetworkPC();
                break;


        }
        return super.onOptionsItemSelected(item);
    }

    public interface SmbConnectionListener {
        void addConnection(boolean edit, String name, String path, String oldname, String oldPath);

        void deleteConnection(String name, String path);
    }

}
