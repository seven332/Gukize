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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

public class LargePagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_large_pager);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new Adapter(getSupportFragmentManager()));
    }


    private class Adapter extends FragmentStatePagerAdapter {

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            PageFragment fragment = new PageFragment();

            final int i = position % 25;
            if (i >= 0 && i <= 11) {
                if (i % 2 == 0) {
                    fragment.setData(Constants.KEY_JPEG, Constants.URL_JPEG);
                } else {
                    fragment.setData(Constants.KEY_PNG, Constants.URL_PNG);
                }
            } else if (i == 24) {
                fragment.setData(Constants.KEY_BAD, Constants.URL_BAD);
            } else {
                fragment.setData(Constants.KEY_GIF, Constants.URL_GIF);
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return 100;
        }
    }
}
