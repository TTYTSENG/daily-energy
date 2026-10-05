package tw.personal.energy;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.webkit.*;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.Toast;
import java.io.*;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public final class MainActivity extends Activity {
 private WebView web;
 private SharedPreferences prefs;
 private static final int EXPORT=41, IMPORT=42;
 private static final String ORIGIN="https://app.local/";
 @Override public void onCreate(Bundle state){
  super.onCreate(state);
  prefs=getSharedPreferences("daily-energy-v1",MODE_PRIVATE);
  getWindow().setStatusBarColor(0xff143b38);
  getWindow().setNavigationBarColor(0xfff4f7f4);
  getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
  web=new WebView(this);
  web.setBackgroundColor(0xfff4f7f4);
  FrameLayout root=new FrameLayout(this);
  root.setBackgroundColor(0xff143b38);
  root.addView(web,new FrameLayout.LayoutParams(-1,-1));
  if(android.os.Build.VERSION.SDK_INT>=30)getWindow().setDecorFitsSystemWindows(false);
  setContentView(root);
  root.setOnApplyWindowInsetsListener((v,insets)->{
   if(android.os.Build.VERSION.SDK_INT>=30){android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars());android.graphics.Insets ime=insets.getInsets(WindowInsets.Type.ime());v.setPadding(bars.left,bars.top,bars.right,Math.max(bars.bottom,ime.bottom));}
   else v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
   return insets.consumeSystemWindowInsets();
  });
  root.requestApplyInsets();
  web.getSettings().setJavaScriptEnabled(true);
  web.getSettings().setDomStorageEnabled(true);
  web.getSettings().setAllowFileAccess(false);
  web.getSettings().setAllowContentAccess(false);
  web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
  web.setWebChromeClient(new WebChromeClient(){
   @Override public boolean onJsConfirm(WebView view,String url,String message,JsResult result){new AlertDialog.Builder(MainActivity.this).setTitle("每日能量").setMessage(message).setPositiveButton("確定",(d,w)->result.confirm()).setNegativeButton("取消",(d,w)->result.cancel()).setOnCancelListener(d->result.cancel()).show();return true;}
   @Override public boolean onJsAlert(WebView view,String url,String message,JsResult result){new AlertDialog.Builder(MainActivity.this).setTitle("每日能量").setMessage(message).setPositiveButton("確定",(d,w)->result.confirm()).setOnCancelListener(d->result.confirm()).show();return true;}
  });
  web.addJavascriptInterface(new LocalStore(),"Native");
  web.setWebViewClient(new WebViewClient(){
   @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){
    Uri u=request.getUrl();
    if(!"https".equals(u.getScheme())||!"app.local".equals(u.getHost()))return blocked();
    String path=u.getPath();if(path==null||path.contains(".."))return blocked();
    if("/".equals(path))path="/index.html";
    String mime=path.endsWith(".js")?"application/javascript":path.endsWith(".css")?"text/css":"text/html";
    try{return new WebResourceResponse(mime,"UTF-8",getAssets().open(path.substring(1)));}catch(IOException e){return blocked();}
   }
   @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){
    Uri u=request.getUrl();if(ORIGIN.equals(u.toString()))return false;
    if("https".equals(u.getScheme())&&!"app.local".equals(u.getHost()))try{startActivity(new Intent(Intent.ACTION_VIEW,u));}catch(Exception e){Toast.makeText(MainActivity.this,"找不到瀏覽器",Toast.LENGTH_SHORT).show();}
    return true;
   }
  });
  web.loadUrl(ORIGIN+"index.html");
 }
 private WebResourceResponse blocked(){return new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));}
 public final class LocalStore {
  @JavascriptInterface public String load(){return prefs.getString("state","");}
  @JavascriptInterface public boolean save(String value){if(value.length()>10000000)return false;return prefs.edit().putString("state",value).commit();}
  @JavascriptInterface public void exportFile(String name,String mime,String text){runOnUiThread(()->{try(FileOutputStream out=new FileOutputStream(new File(getCacheDir(),"pending-export"))){out.write(text.getBytes(StandardCharsets.UTF_8));Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType(mime);i.putExtra(Intent.EXTRA_TITLE,name);startActivityForResult(i,EXPORT);}catch(Exception e){toast("無法匯出檔案，請檢查儲存空間及檔案選擇器");}});}
  @JavascriptInterface public void importFile(){runOnUiThread(()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");try{startActivityForResult(i,IMPORT);}catch(Exception e){toast("此手機無法開啟檔案選擇器");}});}
 }
 @Override protected void onActivityResult(int code,int result,Intent data){
  super.onActivityResult(code,result,data);
  if(result!=RESULT_OK||data==null||data.getData()==null)return;
  Uri uri=data.getData();
  try{
   if(code==EXPORT){File pending=new File(getCacheDir(),"pending-export");try(InputStream in=new FileInputStream(pending);OutputStream out=getContentResolver().openOutputStream(uri,"wt")){if(out==null)throw new IOException();byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);}pending.delete();toast("已匯出檔案");}
   else if(code==IMPORT){try(InputStream in=getContentResolver().openInputStream(uri);ByteArrayOutputStream out=new ByteArrayOutputStream()){
    if(in==null)throw new IOException();byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1){out.write(buf,0,n);if(out.size()>10000000)throw new IOException("too large");}
    String text=out.toString("UTF-8");web.evaluateJavascript("window.onBackupLoaded("+JSONObject.quote(text)+")",null);
   }}
  }catch(Exception e){toast("檔案讀寫失敗，請重試或選擇其他位置");}
 }
 private void toast(String message){Toast.makeText(this,message,Toast.LENGTH_LONG).show();}
 @Override public void onBackPressed(){web.evaluateJavascript("window.handleBack()",v->{if(!"true".equals(v))MainActivity.super.onBackPressed();});}
 @Override protected void onDestroy(){if(web!=null){web.removeJavascriptInterface("Native");web.destroy();}super.onDestroy();}
}
