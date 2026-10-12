package com.example.vehicleentry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.*;
import android.view.*;
import android.widget.*;
import java.io.*;

public class MainActivity extends Activity {
    private WebView web;
    private static final int PICK_IMPORT=21, PICK_EXPORT=22;
    private String pendingExport="";
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        web = new WebView(this);
        web.setBackgroundColor(0xffffffff);
        WebSettings s=web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        web.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams params) {
                Intent i=params.createIntent();
                try { startActivityForResult(i, 33); } catch(Exception e) { cb.onReceiveValue(null); }
                fileCallback=cb; return true;
            }
        });
        web.setWebViewClient(new WebViewClient());
        web.addJavascriptInterface(new Object(){
            @JavascriptInterface public void exportBackup(String json){ pendingExport=json; runOnUiThread(()->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,"车辆临时入校申请备份.json");startActivityForResult(i,PICK_EXPORT);});}
            @JavascriptInterface public void openMenu(){runOnUiThread(()->showMenu());}
            @JavascriptInterface public void saveNow(){ runOnUiThread(()->web.evaluateJavascript("save()",null)); }
            @JavascriptInterface public void toast(String t){ runOnUiThread(()->Toast.makeText(MainActivity.this,t,Toast.LENGTH_SHORT).show()); }
        },"AndroidApp");
        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
    }
    private ValueCallback<Uri[]> fileCallback;
    private void showMenu(){
        final String[] items={"保存","导出备份","导入备份","清空内容"};
        new AlertDialog.Builder(this).setItems(items,(d,which)->{
            if(which==0){web.evaluateJavascript("save()",null);Toast.makeText(this,"已保存到本机",Toast.LENGTH_SHORT).show();}
            else if(which==1)web.evaluateJavascript("exportForAndroid()",null);
            else if(which==2){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("application/json");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK_IMPORT);}
            else new AlertDialog.Builder(this).setMessage("确定清空全部填写内容及照片？").setNegativeButton("取消",null).setPositiveButton("清空",(dd,ww)->web.evaluateJavascript("document.getElementById('clearBtn').click()",null)).show();
        }).show();
    }
    @Override public boolean onCreateOptionsMenu(android.view.Menu menu) {
        menu.add("保存").setOnMenuItemClickListener(item->{web.evaluateJavascript("save()",null);Toast.makeText(this,"已保存到本机",Toast.LENGTH_SHORT).show();return true;});
        menu.add("导出备份").setOnMenuItemClickListener(item->{web.evaluateJavascript("exportForAndroid()",null);return true;});
        menu.add("导入备份").setOnMenuItemClickListener(item->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("application/json");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK_IMPORT);return true;});
        menu.add("清空内容").setOnMenuItemClickListener(item->{new AlertDialog.Builder(this).setMessage("确定清空全部填写内容及照片？").setNegativeButton("取消",null).setPositiveButton("清空",(d,w)->web.evaluateJavascript("document.getElementById('clearBtn').click()",null)).show();return true;});
        return true;
    }
    @Override public boolean onPrepareOptionsMenu(android.view.Menu menu){return super.onPrepareOptionsMenu(menu);}
    @Override public void onBackPressed(){ if(web.canGoBack()) web.goBack(); else new AlertDialog.Builder(this).setMessage("退出申请表？已填写内容会自动保存在本机。").setNegativeButton("继续填写",null).setPositiveButton("退出",(d,w)->finish()).show(); }
    @Override protected void onPause(){ if(web!=null)web.evaluateJavascript("save(true)",null);super.onPause(); }
    @Override protected void onActivityResult(int req,int res,Intent data){
        super.onActivityResult(req,res,data);
        if(req==33 && fileCallback!=null){Uri[] r=(res==RESULT_OK&&data!=null&&data.getData()!=null)?new Uri[]{data.getData()}:null;fileCallback.onReceiveValue(r);fileCallback=null;return;}
        if(res!=RESULT_OK||data==null||data.getData()==null)return;
        Uri u=data.getData();
        try {
            if(req==PICK_IMPORT){InputStream in=getContentResolver().openInputStream(u);ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;while((n=in.read(buf))>0)out.write(buf,0,n);String json=new String(out.toByteArray(),"UTF-8");web.evaluateJavascript("restore("+org.json.JSONObject.quote(json)+")",null);web.evaluateJavascript("save()",null);}
            else if(req==PICK_EXPORT){OutputStream out=getContentResolver().openOutputStream(u);out.write(pendingExport.getBytes("UTF-8"));out.close();Toast.makeText(this,"备份已导出",Toast.LENGTH_SHORT).show();}
        }catch(Exception e){Toast.makeText(this,"文件操作失败："+e.getMessage(),Toast.LENGTH_LONG).show();}
    }

}
