package hawerner.browser;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class JSInterface {

    public JSInterface() {
    }


    public static String checkIfLoginPage(){
        return  "function checkIfLoginPage(){var inputs = document.getElementsByTagName('input');\n" +
                "for(var i = 0; i < inputs.length; i++) {\n" +
                "    if(inputs[i].type.toLowerCase() == 'password') {\n" +
                "        return true;\n" +
                "    }\n" +
                "}return false;}checkIfLoginPage();";
    }

    @JavascriptInterface
    public void test(){}

    public static String fillInputBoxes(String username, String password){
        return "function fillInputBoxes(username, password){var inputs = document.getElementsByTagName('input');\n" +
                "for(var i = 0; i < inputs.length; i++) {\n" +
                "    if(inputs[i].type.toLowerCase() == 'password') {\n" +
                "        inputs[i - 1].value = username;\n" +
                "        inputs[i].value = password;\n" +
                "    }\n" +
                "}}fillInputBoxes('" + username + "', '" + password + "');";
    }
}
