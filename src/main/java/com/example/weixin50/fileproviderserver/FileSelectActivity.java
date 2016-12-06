package com.example.weixin50.fileproviderserver;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.example.weixin50.R;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSelectActivity extends Activity {
    private ExpandableListView mListView;
    private ExpandableListAdapter mListAdapter;
    private File mIntFilesDir, mExtFilesDir;
    File[] mIntFiles, mExtFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.file_select);

        setupFiles();
        //设置listView
        mListView = (ExpandableListView) findViewById(R.id.filelist);
        setupListView();
        setResult(Activity.RESULT_CANCELED, null);
    }

    private void setupFiles() {
        mIntFilesDir = new File(getFilesDir(), "files");
        mIntFiles = mIntFilesDir.listFiles();
        mExtFilesDir = new File(Environment.getExternalStorageDirectory(), "files");
        mExtFiles = mExtFilesDir.listFiles();
    }

    private void setupListView() {
        mListAdapter = (ExpandableListAdapter) new MyFileListAdapter();
        mListView.setAdapter(mListAdapter);
        mListView.setGroupIndicator(null);
        mListView.setOnChildClickListener(mItemClickListener);
    }

    private class MyFileListAdapter extends BaseExpandableListAdapter {
        private Map<String, List<File>> mGroups;
        private final String[] GROUP_NAMES = new String[]{
                "内部文件列表", "外部文件列表"
        };

        public MyFileListAdapter() {
            mGroups = new HashMap<String, List<File>>();
            if (mIntFiles != null) {
                List<File> intFilesList = new ArrayList<File>();
                for (File file : mIntFiles) {
                    intFilesList.add(file);
                }
                mGroups.put(GROUP_NAMES[0], intFilesList);
            }

            if (mExtFiles != null) {
                List<File> extFilesList = new ArrayList<File>();
                for (File file : mExtFiles) {
                    extFilesList.add(file);
                }
                mGroups.put(GROUP_NAMES[1], extFilesList);
            }
        }

        @Override
        public int getGroupCount() {
            // TODO Auto-generated method stub
            return mGroups.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            // TODO Auto-generated method stub
            return mGroups.get(GROUP_NAMES[groupPosition]).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            // TODO Auto-generated method stub
            return mGroups.get(GROUP_NAMES[groupPosition]);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return mGroups.get(GROUP_NAMES[groupPosition]).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            // TODO Auto-generated method stub
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            if (convertView == null) {
                convertView = View.inflate(FileSelectActivity.this, R.layout.file_select_group, null);
            }

            if (!isExpanded) {
                mListView.expandGroup(groupPosition);
            }

            TextView tvGrpTitle = (TextView) convertView.findViewById(R.id.tvGroupTitle);
            tvGrpTitle.setText(GROUP_NAMES[groupPosition]);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            if (convertView == null) {
                convertView = View.inflate(FileSelectActivity.this, R.layout.file_select_item, null);
            }

            TextView filename = (TextView) convertView.findViewById(R.id.tvFileName);
            File file = mGroups.get(GROUP_NAMES[groupPosition]).get(childPosition);
            filename.setText(file.getName());

            TextView filesize = (TextView) convertView.findViewById(R.id.tvFileSize);
            long bytes = file.length();
            String fileSileInKB = new DecimalFormat("#,###.00").format(bytes / (double) 1024);
            filesize.setText(fileSileInKB + " KB");

            long times = file.lastModified();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(times);
            TextView filedate = (TextView) convertView.findViewById(R.id.tvFileDate);
            filedate.setText(cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-"
                    + cal.get(Calendar.DAY_OF_MONTH) + " " + cal.get(Calendar.HOUR_OF_DAY) + ":" +
                    cal.get(Calendar.MINUTE));
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return true;
        }

    }

    private ExpandableListView.OnChildClickListener mItemClickListener =
            new ExpandableListView.OnChildClickListener() {

                @Override
                public boolean onChildClick(ExpandableListView parent, View v,
                                            int groupPosition, int childPosition, long id) {
                    // TODO Auto-generated method stub
                    File file = (File) parent.getExpandableListAdapter().getChild(groupPosition, childPosition);
                    Uri fileUri;
                    try {
                        fileUri = FileProvider.getUriForFile(
                                FileSelectActivity.this,
                                FileSelectActivity.this.getApplicationContext().getPackageName() + ".provider",
                                file);
                        Intent resultIntent = new Intent();
                        if (fileUri != null) {
                            resultIntent.addFlags(
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            // Put the Uri and MIME type in the result Intent
                            resultIntent.setDataAndType(
                                    fileUri,
                                    getContentResolver().getType(fileUri));
                            // Set the result
                            setResult(Activity.RESULT_OK,
                                    resultIntent);
                        } else {
                            resultIntent.setDataAndType(null, "");
                            setResult(RESULT_CANCELED,
                                    resultIntent);
                        }
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

					/*
					try {
						Method getPathStrategy = FileProvider.class.getDeclaredMethod("getPathStrategy", Context.class, String.class);
						//PathStrategy strategy = (PathStrategy)getPathStrategy.invoke(FileProvider.class, FileSelectActivity.this,
	                     //       getResources().getString(R.string.fileprovider_authority));
						Class<?> targetClass = null;
						Class<?>[] classes = FileProvider.class.getDeclaredClasses();
						for (Class<?> cls : classes) {
							Log.v("FileSelectActivity","FileSelectActivity class.name = "+cls.getName());
							if (cls.getName().equals("android.support.v4.content.FileProvider$SimplePathStrategy")) {
								targetClass = cls;
								break;
							}
						}
						getPathStrategy.setAccessible(true);
						Object obj = getPathStrategy.invoke(FileProvider.class, FileSelectActivity.this,
			                            getResources().getString(R.string.fileprovider_authority));
						Field mRoots = targetClass.getDeclaredField("mRoots");
						mRoots.setAccessible(true);
						HashMap<String, File> roots = (HashMap<String, File>)mRoots.get(obj);
						for (Map.Entry<String, File> root : roots.entrySet()) {
			              Log.v("FileSelectActivity", root.getKey()+":"+ root.getValue().getPath());
			              //Toast.makeText(FileSelectActivity.this, root.getKey()+":"+ root.getValue().getPath(), Toast.LENGTH_LONG).show();
			            }
						//Toast.makeText(FileSelectActivity.this, Environment.get, Toast.LENGTH_LONG).show();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					*/
                    return true;
                }

            };
}
