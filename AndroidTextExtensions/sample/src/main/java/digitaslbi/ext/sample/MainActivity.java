/*
 * Copyright (c) 2015 DigitasLBi.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package digitaslbi.ext.sample;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import digitaslbi.ext.TextView;
import digitaslbi.ext.drawables.MultiDrawablesExtension;
import digitaslbi.ext.drawables.ripple.RippleDrawable;
import digitaslbi.ext.fonts.PermanentMarker;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = (TextView) findViewById(R.id.textView5);
        textView.setFocusable(true);
        textView.setFocusableInTouchMode(true);
        textView.setClickable(true);
        textView.setFont(PermanentMarker.PermanentMarker);
        textView.setPadding(0,200,0,200);
        textView.addDrawable(getResources().getDrawable(R.drawable.example_drawable), MultiDrawablesExtension.EMPTY_RECT, 0);

        int[][] states = new int[][] {
                new int[] { android.R.attr.state_enabled}, // enabled
                new int[] { android.R.attr.state_pressed}  // pressed
        };

        int[] colors = new int[] {
                Color.argb(150,0,255,0),
                Color.argb(150,0,0,255)
        };

        ColorStateList colorStateList = new ColorStateList(states, colors);
        textView.addDrawable(new RippleDrawable(colorStateList), MultiDrawablesExtension.EMPTY_RECT, 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
