package com.example.android.sunshine;

import android.content.Intent;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DetailedActivity extends AppCompatActivity {
    TextView textView;
    String s;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent extra=getIntent();
        s=extra.getStringExtra("String");
        textView=(TextView)findViewById(R.id.tv);
        textView.setText(s);
    }

    public void share(){
        String mime="text/plain";
        String title="Sharing";
        String data=s;
        Intent intent=ShareCompat.IntentBuilder.from(this)
                .setChooserTitle(title)
                .setType(mime)
                .setText(data).getIntent();
        if(intent.resolveActivity(getPackageManager())!=null)
            startActivity(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detailed,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_share:{
                share();
            return true;
            }
            case R.id.settings_action:{
                Intent intent=new Intent(DetailedActivity.this,Settings_activity.class);
                startActivity(intent);
            }
            default: return super.onOptionsItemSelected(item);
        }
    }
}
