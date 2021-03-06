package com.spacECE.spaceceedu.Consultants;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spacECE.spaceceedu.UsefulFunctions;
import com.spacECE.spaceceedu.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


public class Consultant_Categories extends Fragment {

    private ArrayList<ConsultantCategory> categories=new ArrayList<>();
    private RecyclerView categoryRecyclerView;
    private Consultant_Categories_RecyclerAdapter adapter;
    private Consultant_Categories_RecyclerAdapter.RecyclerViewClickListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_consultant__categories, container, false);

        Log.i("Categories", "Initiated");
//        generateList();

        categoryRecyclerView = v.findViewById(R.id.Consultant_Category_RecyclerView);

        categories = Consultant_Main.categoryList;

        setAdapter(categories);

        return v;
    }


    private void setAdapter(ArrayList<ConsultantCategory> myList) {
        Log.i("SetAdapter:", "Working");
        setOnClickListener();
        adapter = new Consultant_Categories_RecyclerAdapter(myList, listener);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 2, LinearLayoutManager.VERTICAL, false);
        categoryRecyclerView.setLayoutManager(layoutManager);
        categoryRecyclerView.setItemAnimator(new DefaultItemAnimator());
        categoryRecyclerView.setAdapter(adapter);
        Log.i("Adapter", "Executed");
    }

    private void setOnClickListener() {
        listener = new Consultant_Categories_RecyclerAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View v, int position) {
                getList(categories.get(position).getCategoryName(), ConsultantsLibrary.consultantsList);
                Intent intent = new Intent(getContext(), ConsultantsLibrary.class);
                startActivity(intent);
            }
        };
    }

    public void getList(String category, ArrayList<Consultant> aList) {
        final boolean[] COMPLETED = {false};
        final JSONObject[] apiCall = {null};

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                try{
                    try {
                        apiCall[0] = UsefulFunctions.UsingGetAPI("http://educationfoundation.space/ConsultUs/api_category?category=one&val=" + URLEncoder.encode(category, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    try {
                        Log.i("Object Obtained: ", apiCall[0].get("data").toString());
                    } catch (JSONException e) {
                        Log.i("API Response:", "Error");
                        e.printStackTrace();
                    }

                    JSONArray jsonArray = null;
                    try {
                        jsonArray = apiCall[0].getJSONArray("data");
                        Log.i("API : ", apiCall[0].toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    try {
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject response_element = new JSONObject(String.valueOf(jsonArray.getJSONObject(i)));

                            Consultant newConsultants = new Consultant(response_element.getString("name"), response_element.getString("c_id"), response_element.getString("img")
                                    , response_element.getString("category"), response_element.getString("office"), response_element.getString("mobile"), response_element.getString("lang")
                                    , response_element.getString("email"), response_element.getString("ctime"), response_element.getString("stime"), response_element.getString("lno")
                                    , response_element.getString("available_to"), response_element.getString("available_from"), response_element.getString("qualification"), response_element.getString("fee"));

                            aList.add(newConsultants);
                        }
                        COMPLETED[0] = true;
                        Log.i("CONSULTANTS ::::: ===>", aList.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        COMPLETED[0] = true;
                    }

                }catch(RuntimeException e){
                    Log.i("RUNTIME ERROR::::", "Server took too long...");
                    COMPLETED[0] = true;
                }
            }
        });

        thread.start();
    }

//    private void generateList() {
//        Log.i("Generate List : "," Generating....");
//        categories.add(new ConsultantCategory("Psycho","Nice"));
//        categories.add(new ConsultantCategory("Surgeon","Nice"));
//        categories.add(new ConsultantCategory("Anesthesia","Nice"));
//        categories.add(new ConsultantCategory("New","Nice"));
//        categories.add(new ConsultantCategory("Old","Nice"));
//        categories.add(new ConsultantCategory("Good","Nice"));
//        categories.add(new ConsultantCategory("Great","Nice"));
//        categories.add(new ConsultantCategory("Cool","Nice"));
//        categories.add(new ConsultantCategory("Kind","Nice"));
//        categories.add(new ConsultantCategory("Unkind","Nice"));
//        categories.add(new ConsultantCategory("Right","Nice"));
//        categories.add(new ConsultantCategory("Down","Nice"));
//        categories.add(new ConsultantCategory("Up","Nice"));
//        setAdapter(categories);
//    }

}
