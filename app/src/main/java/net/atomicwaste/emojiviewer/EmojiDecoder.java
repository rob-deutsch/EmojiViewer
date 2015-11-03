package net.atomicwaste.emojiviewer;

import android.app.AlertDialog;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class EmojiDecoder extends AppCompatActivity {
    public SparseIntArray emojiMap;
    public SparseArray unicodeMap;
    public SparseArray ignoreMap;

    @Override
    protected void onPostResume() {
        fromClipboard(null);
        super.onPostResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emoji_decoder);

        // Read in emoji mapping
        // Open the CSV
        getEmojiMap();
        getUnicodeMap();
        getIgnoreMap();

        // Set up watcher
        EditText enterMessage = (EditText) findViewById(R.id.enter_message);
        enterMessage.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                translateMessage();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        enterMessage.setText("test");

    }

    private void getEmojiMap() {
        emojiMap = new SparseIntArray();
        try {
            AssetManager am = getAssets();
            InputStream is = am.open("emoji.csv");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            br.readLine(); // Read the headings line first
            while (br.ready()) {
                String line = br.readLine();
                String[] values = line.split(",");
                emojiMap.put(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
            }
            is.close();
        }
        catch (IOException ex) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle("Error")
                    .setMessage("Couldn't find emoji.csv")
                    .setIcon(android.R.drawable.ic_dialog_alert);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }
    
    private void getUnicodeMap() {
        unicodeMap = new SparseArray();
        try {
            AssetManager am = getAssets();
            InputStream is = am.open("unicode_70_80.csv");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            br.readLine(); // Read the headings line first
            while (br.ready()) {
                String line = br.readLine();
                String[] values = line.split(",");
                unicodeMap.put(Integer.parseInt(values[0]), values[1]);
            }
            is.close();
        }
        catch (IOException ex) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle("Error")
                    .setMessage("Couldn't find unicode_70_80.csv")
                    .setIcon(android.R.drawable.ic_dialog_alert);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void getIgnoreMap() {
        ignoreMap = new SparseArray();
        try {
            AssetManager am = getAssets();
            InputStream is = am.open("ignore.csv");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            br.readLine(); // Read the headings line first
            while (br.ready()) {
                String line = br.readLine();
                String[] values = line.split(",");
                ignoreMap.put(Integer.parseInt(values[0]), values[1]);
            }
            is.close();
        }
        catch (IOException ex) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle("Error")
                    .setMessage("Couldn't find unicode_70_80.csv")
                    .setIcon(android.R.drawable.ic_dialog_alert);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void fromClipboard(View view) {        // Get the string
        TextView enterMessage = (TextView) findViewById(R.id.enter_message);
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (cm.hasPrimaryClip()) {
                String text = cm.getPrimaryClip().getItemAt(0).getText().toString();
                enterMessage.setText(text);

        } else {
            enterMessage.setText(R.string.no_text_in_clipbard);
        }
        translateMessage();
    }

    public void translateMessage() {
        // Get the string
        EditText enterMessage = (EditText) findViewById(R.id.enter_message);
        TextView showMessage = (TextView) findViewById(R.id.show_message);
        String s = enterMessage.getText().toString();

        // Count the number of characters in the string
        int countCodePoints = s.codePointCount(0, s.length());

        // Collect the codepoints of all the characters
        String proper_emoji = "";
        int offset = 0, strLen = s.length();
        while (offset < strLen) {
            int cp = s.codePointAt(offset);
            offset += Character.charCount(cp);
            cp = emojiMap.get(cp, cp);
            proper_emoji += new String(Character.toChars(cp));
        }
        
        // Collect the codepoints of all the characters
        String new_unicode = "";
        offset = 0; strLen = proper_emoji.length();
        while (offset < strLen) {
            int cp = proper_emoji.codePointAt(offset);
            offset += Character.charCount(cp);
            String orig_char = new String(Character.toChars(cp));
            Object thing = unicodeMap.get(cp);
            if (thing != null) {
                thing = "(" + thing + ")";
            } else {
                thing = orig_char;
            }
            new_unicode += thing;
        }

        // Collect the codepoints of all the characters
        String output = "";
        offset = 0; strLen = new_unicode.length();
        while (offset < strLen) {
            int cp = new_unicode.codePointAt(offset);
            offset += Character.charCount(cp);
            String orig_char = new String(Character.toChars(cp));
            Object thing = ignoreMap.get(cp);
            if (thing != null) {
                thing = "";
            } else {
                thing = orig_char;
            }
            output += thing;
        }

        // Output to the text box
        showMessage.setText(output);
    }


}
