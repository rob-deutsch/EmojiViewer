package net.atomicwaste.emojiviewer;

import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class EmojiDecoder extends AppCompatActivity {
    public SparseIntArray emojiMap;

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
            return;
        }

        // Set up watcher
        EditText enterMessage = (EditText) findViewById(R.id.enter_message);
        enterMessage.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                translateMessage();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
    }

    public void fromClipboard(View view) {        // Get the string
        TextView enterMessage = (TextView) findViewById(R.id.enter_message);
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (cm.hasPrimaryClip()) {
            if (cm.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                String text = cm.getPrimaryClip().getItemAt(0).getText().toString();
                enterMessage.setText(text);
            } else {
                enterMessage.setText(R.string.no_text_in_clipbard);
            }
        } else {
            enterMessage.setText(R.string.no_text_in_clipbard);
        }
        //translateMessage();
    }
    public void translateMessage() {
        // Get the string
        EditText enterMessage = (EditText) findViewById(R.id.enter_message);
        TextView showMessage = (TextView) findViewById(R.id.show_message);
        String s = enterMessage.getText().toString();

        // Count the number of characters in the string
        int countCodePoints = s.codePointCount(0, s.length());

        // Collect the codepoints of all the characters
        String output = "";
        int offset = 0, strLen = s.length();
        while (offset < strLen) {
            int cp = s.codePointAt(offset);
            offset += Character.charCount(cp);
            cp = emojiMap.get(cp, cp);
            output += new String(Character.toChars(cp));
        }

        // Output to the text box
        showMessage.setText(output);
    }


}
