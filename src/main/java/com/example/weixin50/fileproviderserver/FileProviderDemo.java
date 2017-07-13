package com.example.weixin50.fileproviderserver;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.weixin50.R;

import java.io.File;
import java.io.FileOutputStream;

public class FileProviderDemo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_provider_demo);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private Button btnMakeIntFile, btnMakeExtFile;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);
            initializeViews(rootView);
            return rootView;
        }

        private void initializeViews(View rootView) {
            btnMakeIntFile = (Button) rootView.findViewById(R.id.btnMakeIntFile);
            btnMakeIntFile.setOnClickListener(clicklistener);
            btnMakeExtFile = (Button) rootView.findViewById(R.id.btnMakeExtFile);
            btnMakeExtFile.setOnClickListener(clicklistener);
        }

        private View.OnClickListener clicklistener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (v == btnMakeIntFile) {
                    makeIntFile();
                } else if (v == btnMakeExtFile) {
                    makeExtFile();
                }
            }
        };

        //在内部存储上创建几个测试文件,
        private void makeIntFile() {
            Context context = getActivity();
            boolean success = true;

            //创建目录，通常是/data/data/<包名>/files/files
            File file = new File(context.getFilesDir(), "files");
            if (!file.exists()) {
                file.mkdirs();
            }

            //在上面的目录下创建10个文件，用来与客户端应用共享
            for (int i = 0; i < 10; i++) {
                String filename = "微信内部文件" + i + ".txt";
                String content = "这是第" + i + "个内部文件";
                FileOutputStream fos;

                try {
                    fos = new FileOutputStream(new File(file, filename));
                    fos.write(content.getBytes());
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    success = false;
                }
            }

            Toast.makeText(getActivity(), "在" + file.getAbsolutePath() + "目录下创建文件" + (success ? "成功" : "失败"), Toast.LENGTH_SHORT).show();
        }

        //在外部存储上创建几个测试文件
        private void makeExtFile() {
            Context context = getActivity();

            //创建目录
            File file = new File(Environment.getExternalStorageDirectory(), "files");
            if (!file.exists()) {
                file.mkdirs();
            }

            if (isExternalStorageWritable()) {
                boolean success = true;
                //在上面的目录下创建10个文件，用来与客户端应用共享
                for (int i = 0; i < 10; i++) {
                    String filename = "外部文件" + i + ".txt";
                    String content = "这是第" + i + "个外部文件";
                    FileOutputStream fos;

                    try {
                        fos = new FileOutputStream(new File(file, filename));
                        fos.write(content.getBytes());
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        success = false;
                    }
                }

                Toast.makeText(getActivity(), "在" + file.getAbsolutePath() + "目录下创建文件" + (success ? "成功" : "失败"), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "外部存储不可写", Toast.LENGTH_SHORT).show();
            }
        }

        /* 检查外部存储是否可读写 */
        public boolean isExternalStorageWritable() {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                return true;
            }
            return false;
        }
    }

}
