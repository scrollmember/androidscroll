package jp.ac.tokushima_u.is.ll.util;

import com.google.api.translate.Language;
import com.google.api.translate.Translator;

public class GooGleTranslateUtil {
	private static Translator translator;
	
	private static Translator  getInstance(){
		if(translator == null)
			translator = new Translator();
		return translator;
	}

	public static String translate(String content,Language fromlan, Language tolan) {
		String value = ""; 
		try{
			value = getInstance().translate(content, fromlan, tolan);
		}catch(Exception e){
			
		}
		return value;
	}
}
