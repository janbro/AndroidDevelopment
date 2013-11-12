package com.janbrostudios.torrentscanner;

import java.util.ArrayList;
import java.util.Locale;

import org.jsoup.nodes.Element;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    public static final String googleApiKey = "AIzaSyBhXcNgl1iWWa68HYzW0-IxROWidJCfYGQ";
	final Context context = this;
	private static ArrayList<String> productList = new ArrayList<String>();
	IntentIntegrator intent = new IntentIntegrator();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_scan:
	        	intent.initiateScan(this);
	            return true;
	        case R.id.action_clear:
	            productList.clear();
	            showText("");
	            return true;
	        case R.id.action_exit:
	        	productList.clear();
	        	finish();
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch(requestCode) {
		case IntentIntegrator.REQUEST_CODE: {
			if (resultCode != RESULT_CANCELED) {
				IntentResult scanResult;
				try{
					scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
				}finally{}
				if (scanResult != null) {
					String category="none";
					String upc = scanResult.getContents();
					String productTitle=null;
					//Debugging Purposes
					//isbn = "9780805072549";
					//upc = "710425491016";
					if(upc.startsWith("978")||upc.startsWith("979")){
						category="books";
						if(NetworkInterfacing.searchBookTitle(upc)!=null){
							try {
								productTitle = NetworkInterfacing.searchBookTitle(upc);
							  } catch (Exception ex) {
								  ex.printStackTrace();
							  }
						}
					}
					if(productTitle==null)
						productTitle = NetworkInterfacing.searchProductTitle(upc);

					if(productTitle==null){
						Toast toast = Toast.makeText(context, "Product not found!", Toast.LENGTH_SHORT);
						toast.show();
					}
					else{
						Log.d("DEBUG",productTitle);
						Log.d("DEBUG", upc);
						if(productTitle.contains(upc)){
							Log.d("DEBUG","Inside");
							productTitle = productTitle.replace(upc, "");
						}
						verifyProduct(productTitle,category);
					}
				}else{
					Toast toast = Toast.makeText(context, "Could not open camera!", Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		break;		
		}
	}
	}
    
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            Fragment fragment = new DummySectionFragment();
            Bundle args = new Bundle();
            args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
            TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
            dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }
    private ArrayList<Element> getProductLinks(String productTitle,String category){
		ArrayList<Element> katLinks = NetworkInterfacing.getKATInfo(productTitle,category);
		
		return katLinks;
	}
	
	private String getProductInfo(ArrayList<Element> katLinks,int numLinks,String productTitle){
		if(katLinks==null)
			return "1: "+productTitle+"\nNo torrent found.\n\n";
		ArrayList<String> elementNames = new ArrayList<String>();
		ArrayList<String> links = new ArrayList<String>();
		
		for(Element e:katLinks){
			elementNames.add(NetworkInterfacing.getKatElementName(e));
			links.add(NetworkInterfacing.getKatElementDownloadLink(e));
		}
		String result="";
		for(int i=0;i<elementNames.size();i++){
			if(i>=numLinks)
				break;
			result+=(i+1)+": Torrent: "+elementNames.get(i)+"\nLink: "+links.get(0)+"\n\n";
		}
		return result;
	}
	private void showText(String result){
		//put whatever you want to do with the code here
		TextView tv = new TextView(this);
		tv.setLinksClickable(true);
		tv.setText(result);
		Linkify.addLinks(tv, Linkify.ALL);
		setContentView(tv);
	}
	
	private void verifyProduct(final String productTitle,final String category){
		String products="";
		for(String str:productList)
			products+=str+"\n";
		products+=productTitle;
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 
			// set title
			alertDialogBuilder.setTitle("Verify Product");
 
			// set dialog message
			alertDialogBuilder
				.setMessage(products)
				.setCancelable(false)
				.setPositiveButton("Done",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						ProgressDialog progress = new ProgressDialog(context);
						progress.setTitle("Loading");
						progress.setMessage("Wait while loading...");
						progress.show();
						productList.add(productTitle);
						// if this button is clicked, close
						// current activity
						dialog.cancel();
						String finalText="";
						if(productList.size()>1)
							for(String prod:productList){
								finalText+=getProductInfo(getProductLinks(prod,category),1,prod);
							}
						else{
							ArrayList<Element> downloadLinks = getProductLinks(productTitle,category);
							if(downloadLinks!=null)
								finalText=getProductInfo(downloadLinks,downloadLinks.size(),productTitle);
							else{
								finalText=productTitle+"\nNo torrent found!";
								Toast toast = Toast.makeText(context, "No torrent not found!", Toast.LENGTH_LONG);
								toast.show();
								productList.clear();
							}
						}
						progress.dismiss();
						showText(finalText);
					}
				  })
				.setNeutralButton("Next Scan", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						productList.add(productTitle);
						// TODO Auto-generated method stub
						dialog.cancel();
						IntentIntegrator.initiateScan(MainActivity.this);
					}
						
				})
				  .setNegativeButton("Restart Scan",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						productList.clear();
						IntentIntegrator.initiateScan(MainActivity.this);
					}
				});
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
	}
}
