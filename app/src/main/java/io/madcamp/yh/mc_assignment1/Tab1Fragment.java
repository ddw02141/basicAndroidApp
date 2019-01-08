package io.madcamp.yh.mc_assignment1;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import org.json.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import android.database.Cursor;
import android.widget.Toast;

import io.madcamp.yh.mc_assignment1.Retrofit.IMyService;
import io.madcamp.yh.mc_assignment1.Retrofit.RetrofitClient;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class Tab1Fragment extends Fragment {
    /* --- Constants --- */
    public static final String ARG_PAGE = "ARG_PAGE";
    public static final int REQUEST_CODE_ADD = 524;
    public static final int REQUEST_CODE_EDIT = 47;
    public static final int REQUEST_CODE_JSON = 11;
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 112;


    /* --- Member Variables --- */
    private int page;
    private Context context;
    private View top;
    public String[] call_or_delete = {"통화","수정","삭제"};

    public ArrayList<Pair<String, String>> contacts;
    private ArrayList<ListViewAdapter.Item> shownContacts;
    private ListViewAdapter adapter;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IMyService IMyService;

    @Override
    public void onStop(){
        compositeDisposable.clear();
        super.onStop();
    }

    /* --- Header --- */
    /* TabPagerAdapter에서 Fragment 생성할 때 사용하는 메소드 */
    public static Tab1Fragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        Tab1Fragment fragment = new Tab1Fragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt(ARG_PAGE);

        //Init Service
        Retrofit retrofitClient = RetrofitClient.getInstance();
        IMyService = retrofitClient.create(IMyService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        top = inflater.inflate(R.layout.fragment_tab1, container, false);
        this.context = top.getContext();

        initializeFloatingActionButton();

        final ListView contact_listview = (ListView)top.findViewById(R.id.contact_listview);

        String fileContents = readInternalFile("contacts.json");
        contacts = unpackFromJSON(fileContents);
        shownContacts = new ArrayList<>();

        adapter = new ListViewAdapter(context,R.layout.item_text2, shownContacts);
        contact_listview.setAdapter(adapter);

        updateContacts();

        contact_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int shownPosition, long id) {
                final int position = shownContacts.get(shownPosition).index;

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(contacts.get(position).first);
                builder.setItems(call_or_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 2){
                            contacts.remove(position);
                            updateContacts();
                        } else if (which == 1){
                            Intent intent = new Intent(context.getApplicationContext(), EditcontactActivity.class);
                            intent.putExtra("contact_name",contacts.get(position).first);
                            intent.putExtra("contact_number",contacts.get(position).second);
                            intent.putExtra("contact_position",position);
                            startActivityForResult(intent, REQUEST_CODE_EDIT);
                        } else {
                            Intent intent = new Intent("android.intent.action.DIAL",Uri.parse("tel:" + contacts.get(position).second));
                            startActivity(intent);
                        }
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        EditText editText = (EditText)top.findViewById(R.id.searcheditText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateContacts();
            }
        });

        return top;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }



    /* --- Floating Action Button --- */
    // isFabOpen: contains whether FloatingActionButton is opened(true) or not(false)
    private boolean isFabOpen = false;
    public void initializeFloatingActionButton() {
        final FloatingActionButton[] fab = new FloatingActionButton[7];

        /* Find every floating action buttons */
        fab[0] = (FloatingActionButton)top.findViewById(R.id.fab);
        fab[1] = (FloatingActionButton)top.findViewById(R.id.fab1);
        fab[2] = (FloatingActionButton)top.findViewById(R.id.fab2);
        fab[3] = (FloatingActionButton)top.findViewById(R.id.fab3);
        fab[4] = (FloatingActionButton)top.findViewById(R.id.fab4);
        fab[5] = (FloatingActionButton)top.findViewById(R.id.fab5);
        fab[6] = (FloatingActionButton)top.findViewById(R.id.fab6);


        if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
        } else{
            ActivityCompat.requestPermissions(getActivity(),new String[] {Manifest.permission.READ_CONTACTS},1);
        }

        /* Initialize isFabOpen as Closed */
        isFabOpen = false;

        /* Add onClickListener for every floating action buttons */
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                Intent intent;
                switch(id) {
                    case R.id.fab: /* Menu button */
                        anim();
                        break;
                    case R.id.fab1: /* Add button */
                        intent = new Intent(context.getApplicationContext(), AddcontactActivity.class);
                        startActivityForResult(intent, REQUEST_CODE_ADD);
                        break;
                    case R.id.fab2: /* Load from contacts button */

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        builder.setMessage("휴대폰에 저장된 모든 연락처를 목록에 추가합니다. 진행하시겠습니까?");
                        builder.setTitle("경고!")
                                .setCancelable(false)
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String[] arrProjection = {
                                                    ContactsContract.Contacts._ID,
                                                    ContactsContract.Contacts.DISPLAY_NAME};
                                            String[] arrPhoneProjection = {ContactsContract.CommonDataKinds.Phone.NUMBER};

                                            Cursor clsCursor = context.getContentResolver().query(
                                                    ContactsContract.Contacts.CONTENT_URI, arrProjection,
                                                    ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1", null, null);

                                            while(clsCursor.moveToNext()) {
                                                String strContactId = clsCursor.getString(0);

                                                Cursor clsPhoneCursor = context.getContentResolver().query(
                                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, arrPhoneProjection,
                                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + strContactId, null, null);
                                                while(clsPhoneCursor.moveToNext()){
                                                    contacts.add(new Pair<String, String>(clsCursor.getString(1),clsPhoneCursor.getString(0)));
                                                }
                                                clsPhoneCursor.close();
                                                updateContacts();
                                            }
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();

                        break;

                    case R.id.fab3: /* Load from/save as JSON button */
                        intent = new Intent(context.getApplicationContext(), JsoncontactActivity.class);
                        intent.putExtra("JSON", packIntoJSON(contacts));
                        startActivityForResult(intent, REQUEST_CODE_JSON);
                        break;

                    case R.id.fab4:
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());

                        builder2.setMessage("휴대폰에 저장된 모든 연락처를 삭제합니다. 진행하시겠습니까?");
                        builder2.setTitle("경고!")
                                .setCancelable(false)
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        contacts.clear();
                                        updateContacts();
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert2 = builder2.create();
                        alert2.show();
                        break;


                        // 서버에 연락처 저장하기 lets use "addcontacts" on click
                        // 이미 저장된 private ArrayList<Pair<String, String>> contacts 의 정보를 db contactsnodejs에 저장
                    case R.id.fab5:
                        AlertDialog.Builder builder3 = new AlertDialog.Builder(getActivity());

                        builder3.setMessage("서버의 연락처를 초기화 후 기기의 모든 연락처를 동기화 합니다. 진행하시겠습니까?");
                        builder3.setTitle("알림")
                                .setCancelable(false)
                                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        addContacts(contacts);
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert3 = builder3.create();
                        alert3.show();
                        break;

                        // 서버에서 연락처 불러오기
                    case R.id.fab6:
                        AlertDialog.Builder builder4 = new AlertDialog.Builder(getActivity());

                        builder4.setMessage("초기화 후 서버의 모든 연락처를 불러옵니다. 진행하시겠습니까?");
                        builder4.setTitle("알림")
                                .setCancelable(false)
                                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        getContacts(contacts);
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert4 = builder4.create();
                        alert4.show();
                }
            }

            private void addContacts(ArrayList<Pair<String,String>> contacts){

                String j = packIntoJSON(contacts);
                compositeDisposable.add(IMyService.addContacts(j)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>(){

                            @Override
                            public void accept(String response) throws Exception{
                                Toast.makeText(getActivity(), "서버에 모든 연락처가 저장되었습니다",Toast.LENGTH_SHORT).show();
                            }
                        }));
            }

            private void getContacts(final ArrayList<Pair<String,String>> contacts){
                contacts.clear();
                String j = packIntoJSON(contacts);
                compositeDisposable.add(IMyService.getContacts(j)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>(){

                            @Override
                            public void accept(String response) throws Exception{
                                Toast.makeText(getActivity(), "불러오기 완료",Toast.LENGTH_SHORT).show();



                                ArrayList<Pair<String, String>> newList = unpackFromJSON(response);
                                contacts.clear();
                                contacts.addAll(newList);
                                updateContacts();
                            }
                        }));

            }


            /* Helper method for animation */
            private void anim() {
                if(isFabOpen) {
                    for(int i = 1; i < fab.length; i++) {
                        Animation a = AnimationUtils.loadAnimation(context, R.anim.fab_close);
                        a.setStartOffset((fab.length - i - 1) * 50);
                        fab[i].startAnimation(a);
                        fab[i].setClickable(false);
                    }
                    isFabOpen = false;
                } else {
                    for(int i = 1; i < fab.length; i++) {
                        Animation a = AnimationUtils.loadAnimation(context, R.anim.fab_open);
                        a.setStartOffset((i - 1) * 50);
                        fab[i].startAnimation(a);
                        fab[i].setClickable(true);
                    }
                    isFabOpen = true;
                }
            }
        };

        for(int i = 0; i < 7; i++) {
            fab[i].setOnClickListener(onClickListener);
        }
    }


    /* --- ListView --- */
    @Override
    public void onActivityResult(int requestCode,int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        switch(requestCode){
            case REQUEST_CODE_ADD:
                if(resultCode == Activity.RESULT_OK){
                    String contact_name = data.getStringExtra("contact_name");
                    String contact_num = data.getStringExtra("contact_num");
                    if(contact_name == null || contact_num == null) return;
                    Pair<String, String> pair = new Pair<String, String>(contact_name, contact_num);
                    Log.d("Test@", "Pair Created 1st=" + contact_name + ", 2nd=" + contact_num);
                    contacts.add(pair);
                    updateContacts();
                }
            break;
            case REQUEST_CODE_EDIT:
                if(resultCode == Activity.RESULT_OK){
                    String contact_name = data.getStringExtra("contact_name");
                    String contact_num = data.getStringExtra("contact_num");
                    if(contact_name == null || contact_num == null) return;
                    Pair<String, String> pair = new Pair<String, String>(contact_name, contact_num);
                    Log.d("Test@", "Pair Created 1st=" + contact_name + ", 2nd=" + contact_num);
                    contacts.set(data.getIntExtra("contact_position",0),pair);
                    updateContacts();
                }
                break;
            case REQUEST_CODE_JSON:
                if(resultCode == Activity.RESULT_OK) {
                    String json = data.getStringExtra("JSON");
                    ArrayList<Pair<String, String>> newList = unpackFromJSON(json);
                    contacts.clear();
                    contacts.addAll(newList);
                    updateContacts();
                }
                break;
        }
    }


    private void updateContacts() {
        writeInternalFile("contacts.json", packIntoJSON(contacts));
        refilterContacts(((EditText)top.findViewById(R.id.searcheditText)).getText().toString());
        Collections.sort(shownContacts);
        adapter.notifyDataSetChanged();
    }

    private void refilterContacts(String pattern) {
        char[] p = pattern.toCharArray();
        shownContacts.clear();
        for(int i = 0; i < contacts.size(); i++) {
            Pair<String, String> x = contacts.get(i);
            SpannableStringBuilder f_name = fuzzyFind(x.first, p);
            SpannableStringBuilder f_number = fuzzyFind(x.second, p);
            if(f_name != null || f_number != null) {
                if(f_name == null) f_name = new SpannableStringBuilder(x.first);
                if(f_number == null) f_number = new SpannableStringBuilder(x.second);
                shownContacts.add(new ListViewAdapter.Item(i, f_name, f_number));
            }
        }
    }

    private SpannableStringBuilder fuzzyFind(String s, char[] p) {
        SpannableStringBuilder res = new SpannableStringBuilder(s);
        int i;
        int j = 0;
        for(i = 0; i < s.length() && j < p.length; i++) {
            if(fuzzyEqual(s.charAt(i), p[j])) {
                res.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                        i, i + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                res.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary)),
                        i, i + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                j++;
            }
        }
        if(j < p.length) return null;
        return res;
    }

    private boolean fuzzyEqual(char a, char p) {
        if(Character.toLowerCase(a) == Character.toLowerCase(p)) /* 영어 ㅕ */
            return true;
        else if(0xac00 <= a && a <= 0xd7a3) { /* 한글 */
            int a_rel = a - 0xac00;
            int jong = a_rel % 28;
            int jung = a_rel / 28 % 21;
            int cho = a_rel / 28 / 21;

            if(0x1100 <= p && p <= 0x11f9) {
                Log.d("fuzzyEqual@p", "" + (int)(p - 0x1100));
                if (p <= 0x1112 && cho == p - 0x1100) return true;
                else return (0x1161 <= p && p <= 0x1175 && jung == p - 0x1161);
            } else if(0x3131 <= p && p <= 0x3163) {
                Log.d("fuzzyEqual@p_ext", "" + (int)(p - 0x3131));
                if(p <= 0x314e && cho == hangulExtToCho(p - 0x3131)) return true;
                else return (0x314f <= p && jung == p - 0x314f);
            }
        }
        return false;
    }

    private int hangulExtToCho(int v) {
        int r = 0;
        switch(v + 1) {
            case 0x1e: case 0x1d: case 0x1c: case 0x1b: case 0x1a: case 0x19: case 0x18: case 0x17: case 0x16: case 0x15:
            case 0x14: r++;
            case 0x13: case 0x12: case 0x11:
            case 0x10: r++; case 0x0f: r++; case 0x0e: r++;case 0x0d: r++;case 0x0c: r++;case 0x0b: r++;case 0x0a: r++;
            case 0x09: case 0x08: case 0x07:
            case 0x06: r++; case 0x05: r++;
            case 0x04:
            case 0x03: r++;
        }
        return v - r;
    }


    /* --- Utility Methods --- */
    /* JSON Parser */
    private String packIntoJSON(ArrayList<Pair<String, String>> arrayList) {
        try {
            JSONArray array = new JSONArray();
            for(Pair<String, String> p : arrayList) {
                JSONObject item = new JSONObject();
                item.put("name", p.first);
                item.put("phoneNumber", p.second);
                array.put(item);
            }
            return array.toString();
        } catch(JSONException e) {
            return "";
        }
    }

    private ArrayList<Pair<String, String>> unpackFromJSON(String src) {
        return appendFromJSON(new ArrayList<Pair<String, String>>(), src);
    }

    private ArrayList<Pair<String, String>> appendFromJSON(ArrayList<Pair<String, String>> arrayList, String src) {
        try {
            JSONArray array = new JSONArray(src);
            for(int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                Pair<String, String> p = new Pair<>(item.getString("name"), item.getString("phoneNumber"));
                arrayList.add(p);
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    /* File Helper */
    private String readInternalFile(String filename) {
        FileInputStream fis;
        InputStreamReader isr;
        BufferedReader br;
        StringBuilder sb;
        String line;
        try {
            fis = context.openFileInput(filename);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            sb = new StringBuilder();
            while((line = br.readLine()) != null) {
                sb.append(line);
            }
            fis.close();
            return sb.toString();
        } catch(Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void writeInternalFile(String filename, String contents) {
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(contents.getBytes());
            fos.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
