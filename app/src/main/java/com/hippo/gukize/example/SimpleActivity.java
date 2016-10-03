/*
 * Copyright 2016 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.gukize.example;

/*
 * Created by Hippo on 10/3/2016.
 */

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.hippo.gukize.GukizeView;
import com.hippo.konwidget.AdvImageView;

public class SimpleActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 1000;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                GukizeView image;
                if (convertView == null) {
                    convertView = new GukizeView(SimpleActivity.this);
                    Resources resources = getResources();
                    image = (GukizeView) convertView;
                    image.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 512));
                    image.setPlaceholderDrawable(resources.getDrawable(R.drawable.placeholder));
                    image.setFailureDrawable(resources.getDrawable(R.drawable.failure));
                    image.setRetryType(GukizeView.RETRY_TYPE_CLICK);
                    //image.setAutoStart(false);
                } else {
                    image = (GukizeView) convertView;
                }

                switch (position % 2) {
                    case 0:
                        image.setActualScaleType(AdvImageView.SCALE_TYPE_CENTER);
                        image.setFailureScaleType(AdvImageView.SCALE_TYPE_FIT_XY);
                        break;
                    case 1:
                        image.setActualScaleType(AdvImageView.SCALE_TYPE_FIT_XY);
                        image.setFailureScaleType(AdvImageView.SCALE_TYPE_CENTER);
                        break;
                }

                final int i = position % 25;
                if (i >= 0 && i <= 11) {
                    if (i % 2 == 0) {
                        image.load(Constants.KEY_JPEG, Constants.URL_JPEG);
                    } else {
                        image.load(Constants.KEY_PNG, Constants.URL_PNG);
                    }
                } else if (i == 24) {
                    image.load(Constants.KEY_BAD, Constants.URL_BAD);
                } else {
                    image.load(Constants.KEY_GIF, Constants.URL_GIF);
                }

                return convertView;
            }
        });
    }
}
