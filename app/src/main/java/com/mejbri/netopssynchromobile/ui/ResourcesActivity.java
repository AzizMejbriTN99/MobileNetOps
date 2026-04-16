package com.mejbri.netopssynchromobile.ui;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.mejbri.netopssynchromobile.R;
import com.mejbri.netopssynchromobile.model.TechnicianResource;
import com.mejbri.netopssynchromobile.network.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;

public class ResourcesActivity extends AppCompatActivity {

    private RecyclerView rvResources;
    private ResourcesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);
        rvResources = findViewById(R.id.rvResources);
        adapter = new ResourcesAdapter(id -> deleteResource(id));
        rvResources.setLayoutManager(new LinearLayoutManager(this));
        rvResources.setAdapter(adapter);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAdd).setOnClickListener(v -> showAddDialog());
        loadResources();
    }

    private void loadResources() {
        ApiClient.create(TechnicianApi.class).getResources()
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<List<TechnicianResource>> call,
                                           Response<List<TechnicianResource>> r) {
                        if (r.isSuccessful() && r.body() != null)
                            runOnUiThread(() -> adapter.setData(r.body()));
                    }
                    @Override public void onFailure(Call<List<TechnicianResource>> call, Throwable t) {}
                });
    }

    private void showAddDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_resource, null);
        EditText etName = view.findViewById(R.id.etName);
        EditText etQty  = view.findViewById(R.id.etQuantity);
        EditText etUnit = view.findViewById(R.id.etUnit);
        EditText etNote = view.findViewById(R.id.etNote);

        new AlertDialog.Builder(this)
                .setTitle("Add Resource")
                .setView(view)
                .setPositiveButton("Add", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String qty  = etQty.getText().toString().trim();
                    String unit = etUnit.getText().toString().trim();
                    if (name.isEmpty() || qty.isEmpty()) return;

                    Map<String, Object> body = new HashMap<>();
                    body.put("resourceName", name);
                    body.put("quantity", Integer.parseInt(qty));
                    body.put("unit", unit.isEmpty() ? "units" : unit);
                    body.put("notes", etNote.getText().toString().trim());

                    ApiClient.create(TechnicianApi.class).addResource(body)
                            .enqueue(new Callback<>() {
                                @Override
                                public void onResponse(Call<Map<String, String>> call,
                                                       Response<Map<String, String>> r) {
                                    runOnUiThread(() -> loadResources());
                                }
                                @Override public void onFailure(Call<Map<String, String>> call, Throwable t) {}
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteResource(long id) {
        ApiClient.create(TechnicianApi.class).deleteResource(id)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Map<String, String>> call,
                                           Response<Map<String, String>> r) {
                        runOnUiThread(() -> loadResources());
                    }
                    @Override public void onFailure(Call<Map<String, String>> call, Throwable t) {}
                });
    }
}