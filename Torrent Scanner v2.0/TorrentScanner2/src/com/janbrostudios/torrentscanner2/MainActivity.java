package com.janbrostudios.torrentscanner2;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.janbrostudios.torrentscanner2.SwipeDismissListViewTouchListener.DismissCallbacks;
import com.janbrostudios.torrentscanner2.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MainActivity extends Activity {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = false;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = false;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private static final String googleApiKey = "AIzaSyBhXcNgl1iWWa68HYzW0-IxROWidJCfYGQ";
	private SystemUiHider mSystemUiHider;
	final Context context = this;
	final Activity stuff = this;
    ListView lv=null;
    StableArrayAdapter adapter = null;
    ArrayList<String> products = new ArrayList<String>();
    ArrayList<String> productInfo = new ArrayList<String>();
	
	public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
	    Reader reader = null;
	    reader = new InputStreamReader(stream, "UTF-8");        
	    char[] buffer = new char[len];
	    reader.read(buffer);
	    return new String(buffer);
	}
	
	private String downloadUrl(String myurl) throws IOException {
	    InputStream is = null;
	    // Only display the first 500 characters of the retrieved
	    // web page content.
	    int len = 99999;
	        
	    try {
	        URL url = new URL(myurl);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setReadTimeout(10000 /* milliseconds */);
	        conn.setConnectTimeout(15000 /* milliseconds */);
	        conn.setRequestMethod("GET");
	        conn.setDoInput(true);
	        // Starts the query
	        conn.connect();
	        int response = conn.getResponseCode();
	        is = conn.getInputStream();

	        // Convert the InputStream into a string
	        String contentAsString = readIt(is, len);
	        return contentAsString;
	        
	    // Makes sure that the InputStream is closed after the app is
	    // finished using it.
	    } finally {
	        if (is != null) {
	            is.close();
	        } 
	    }
	}
	
	private class StableArrayAdapter extends ArrayAdapter<String> {

	    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

	    public StableArrayAdapter(Context context, int textViewResourceId,
	        List<String> objects) {
	      super(context, textViewResourceId, objects);
	      for (int i = 0; i < objects.size(); ++i) {
	        mIdMap.put(objects.get(i), i);
	      }
	    }

	    @Override
	    public long getItemId(int position) {
	      String item = getItem(position);
	      return mIdMap.get(item);
	    }

	    @Override
	    public boolean hasStableIds() {
	      return true;
	    }

	  }
	
	class RequestTask extends AsyncTask<String, String, ArrayList<String>>{

	    @Override
	    protected ArrayList<String> doInBackground(String... uri){
	    	URL url=null;
			ArrayList<String> torrents = new ArrayList<String>();
	    	try {
                String result=downloadUrl(uri[0]);
                String upc = uri[0].substring(uri[0].indexOf("keyword=")+8);
    	        String res=result;
    	        for(int i=0;i<4;i++)
    	        	res=res.substring(res.indexOf("name")+7);
    	        res=res.substring(0,res.indexOf("\",\""));
    	        if(res.contains("["))
    	        	res=res.substring(0,res.indexOf("["));
    	        if(res.contains("("))
    	        	res=res.substring(0,res.indexOf("("));
    	        if(res.contains("www.shopping.com")||res.isEmpty()){
    	        	torrents.add("Product not found!");
    	        	return torrents;
    	        }
    	        if(res.contains(upc))
    	        	res=res.substring(0,res.indexOf(upc));
    	        String title=res;
    			try {
    				String category = "none";
    				if(upc.startsWith("978")||upc.startsWith("979"))
    					category="books";
    				String url2 = "http://kickass.to/usearch/"+URLEncoder.encode(res,"UTF-8").replace(" ","%20")+"%20category%3A"+category+"/?field=seeders&sorder=desc";
    				res=Jsoup.parse(new URL(url2), 10000).toString();
    			} catch (UnsupportedEncodingException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}catch (IOException e){
    				torrents.add("Connection timeout!");
    				return torrents;
    			}
    			if(res.indexOf("torrentname")==-1){
    				torrents.add(title+"\nNo torrent found!");
    				return torrents;
    			}
    			String download=res.substring(res.indexOf("Download torrent file")+29);
    			res=res.substring(res.indexOf("torrentname"));
				res=res.substring(res.indexOf("href")+7);
    			torrents.add(res.substring(0,res.indexOf("\"")));
    			String seeds = download.substring(download.indexOf("green")+14);
    			seeds = seeds.substring(0,seeds.indexOf("</td>"));
    			download = download.substring(0,download.indexOf("\""));
    			torrents.add(download+"\n"+"Seeds: "+seeds);
    			torrents.add(0,title);
    			productInfo.add(0,title);
    			return torrents;
            } catch (IOException e) {
               torrents.add("Unable to retrieve web page. Check your internet connection.");
               return torrents;
            }
	    }

	    @Override
	    protected void onPostExecute(ArrayList<String> result) {
	        super.onPostExecute(result);
	        //Do anything with response..
	        if(result.get(0).contains("No torrent found!")||result.contains("Product not found!")||result.contains("Unable to retrieve web page. Check your internet connection.")){
			Toast toast = Toast.makeText(context,result.get(0),Toast.LENGTH_LONG);
			LinearLayout layout = (LinearLayout) toast.getView();
			if (layout.getChildCount() > 0) {
			  TextView tv = (TextView) layout.getChildAt(0);
			  tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
			}
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			TextView title = (TextView) findViewById(R.id.fullscreen_content);
			title.setText("Torrent Scanner");
	        }else{
	        	Toast toast = Toast.makeText(context,"Torrent found!",Toast.LENGTH_LONG);
				LinearLayout layout = (LinearLayout) toast.getView();
				if (layout.getChildCount() > 0) {
				  TextView tv = (TextView) layout.getChildAt(0);
				  tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
				}
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				TextView title = (TextView) findViewById(R.id.fullscreen_content);
				title.setText(result.get(0));
	        }
	        result.remove(0);
	        for(int i=0;i<result.size();i++){
	        	result.set(i,result.get(i)+"\n"+result.get(i+1));
	        	result.remove(i+1);
	        	products.add(0,result.get(i));
	        }

	        lv = (ListView) findViewById(R.id.listView1);
	        adapter = new StableArrayAdapter(context,android.R.layout.simple_list_item_1, products);

	        SwipeDismissListViewTouchListener touchListener =
	                new SwipeDismissListViewTouchListener(
	                        lv,
	                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
	                            @Override
	                            public boolean canDismiss(int position) {
	                                return true;
	                            }

	                            @Override
	                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
	                                for (int position : reverseSortedPositions) {
	                                    adapter.remove(adapter.getItem(position));
		                                productInfo.remove(position);
	                                }
	                                TextView title = (TextView) findViewById(R.id.fullscreen_content);
	                				if(productInfo.size()>0)
	                					title.setText(productInfo.get(0));
	                				else
	                					title.setText("Torrent Scanner");
	                				adapter.notifyDataSetChanged();
	                            }
	                        });
	        lv.setOnTouchListener(touchListener);
	        // Setting this scroll listener is required to ensure that during ListView scrolling,
	        // we don't look for swipes.
	        lv.setOnScrollListener(touchListener.makeScrollListener());
	        
	        
	        lv.setOnItemClickListener(new OnItemClickListener() {
	        	  @Override
	        	  public void onItemClick(AdapterView<?> parent, View view,
	        	    int position, long id) {
	        	    Toast.makeText(getApplicationContext(),
	        	      "Opening Torrent " + productInfo.get(position), Toast.LENGTH_SHORT)
	        	      .show();Intent intent = new Intent(Intent.ACTION_VIEW, 
	        				     Uri.parse(lv.getItemAtPosition(position).toString().substring(lv.getItemAtPosition(position).toString().indexOf("\n")+1,lv.getItemAtPosition(position).toString().indexOf("Seeds: "))));
	      			startActivity(intent);
	        	  }
	        	});
	        lv.setOnItemLongClickListener(new OnItemLongClickListener(){
	            @Override
	            public boolean onItemLongClick (AdapterView<?> parent, View view, int position, long id){
	            	Toast.makeText(getApplicationContext(),
	  	        	      productInfo.get(position), Toast.LENGTH_SHORT)
	  	        	      .show();
	            	return true;
	            }
	        });
	    	lv.setAdapter(adapter);
	    }
	}
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);
		final Button button = (Button) findViewById(R.id.scan_button);
		button.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				//Actions
				Toast toast = Toast.makeText(context, "Scan clicked!", Toast.LENGTH_SHORT);
				toast.show();
				new IntentIntegrator(stuff).initiateScan();
			}
		});

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.scan_button).setOnTouchListener(
				mDelayHideTouchListener);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		//delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent){
		switch(requestCode){
		case IntentIntegrator.REQUEST_CODE:
			if(resultCode==Activity.RESULT_OK){
				IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
				String isbn=result.getContents();
				new RequestTask().execute("http://sandbox.api.ebaycommercenetwork.com/publisher/3.0/json/GeneralSearch?apiKey=78b0db8a-0ee1-4939-a2f9-d3cd95ec0fcc&visitorUserAgent&visitorIPAddress&trackingId=7000610&keyword="+isbn);//"https://www.googleapis.com/shopping/search/v1/public/products?key="+googleApiKey+"&country=US&q="+isbn+"&alt=json");
			}
		}
	}
	
	@Override
	public void onBackPressed()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(stuff);
		// Add the buttons
		builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               // User clicked OK button
		        	   finish();
		           }
		       });
		builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               // User cancelled the dialog
		           }
		       });
		// Set other dialog properties
		builder.setMessage("Are you sure you want to exit?");

		// Create the AlertDialog
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
}
