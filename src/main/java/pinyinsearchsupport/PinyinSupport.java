package pinyinsearchsupport;

import arc.struct.ObjectMap;
import arc.util.Strings;

import java.util.Locale;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

public final class PinyinSupport{
    private static final HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();

    // Cache by displayed text; these strings are reused heavily in selection UIs.
    private static final ObjectMap<String, String> pinyinCache = new ObjectMap<>();

    static{
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
    }

    private PinyinSupport(){
    }

    public static boolean looksLikePinyinQuery(String query){
        if(query == null) return false;

        boolean hasWord = false;
        for(int i = 0, n = query.length(); i < n; i++){
            char c = query.charAt(i);
            if(isCjk(c)) return false;
            if(Character.isLetterOrDigit(c)) hasWord = true;
            // allow common separators used for syllable splits
            if(Character.isLetterOrDigit(c) || c == '\'' || c == ' ' || c == '-' || c == '_' || c == '.'){
                continue;
            }
            // other punctuation is treated as a "not pinyin" query
            return false;
        }
        return hasWord;
    }

    public static boolean matches(String rawQuery, String rawText, boolean fuzzy){
        if(rawQuery == null || rawQuery.isEmpty()) return true;
        if(rawText == null || rawText.isEmpty()) return false;

        // Direct match always works (e.g. typing actual Chinese).
        String textNoColors = Strings.stripColors(rawText);
        String qLower = rawQuery.toLowerCase(Locale.ROOT);
        if(textNoColors.toLowerCase(Locale.ROOT).contains(qLower)) return true;

        String query = normalizeQuery(rawQuery);
        if(query.isEmpty()) return true;

        String pinyin = toPinyinCached(textNoColors);
        if(fuzzy){
            query = applyFuzzy(query);
            pinyin = applyFuzzy(pinyin);
        }
        return pinyin.contains(query);
    }

    /**
     * Normalizes query by:
     * - lowercasing
     * - removing spaces and apostrophes (pin'yin == pinyin)
     * - keeping only [a-z0-9]
     */
    public static String normalizeQuery(String query){
        if(query == null || query.isEmpty()) return "";

        StringBuilder out = new StringBuilder(query.length());
        for(int i = 0, n = query.length(); i < n; i++){
            char c = query.charAt(i);
            if(Character.isLetterOrDigit(c)){
                out.append(Character.toLowerCase(c));
            }
            // ignore separators: spaces, apostrophes, dashes, underscores, dots
        }
        return out.toString();
    }

    public static String toPinyinCached(String text){
        if(text == null || text.isEmpty()) return "";

        String cached = pinyinCache.get(text);
        if(cached != null) return cached;

        String result = toPinyin(text);
        pinyinCache.put(text, result);
        return result;
    }

    /**
     * Converts a mixed string to normalized pinyin+ascii:
     * - CJK -> pinyin (no tones)
     * - digits -> digits
     * - latin letters -> lowercased
     * - other chars -> ignored
     */
    public static String toPinyin(String text){
        if(text == null || text.isEmpty()) return "";

        StringBuilder out = new StringBuilder(text.length() * 2);
        for(int i = 0, n = text.length(); i < n; i++){
            char c = text.charAt(i);

            if(isCjk(c)){
                String[] arr;
                try{
                    arr = PinyinHelper.toHanyuPinyinStringArray(c, format);
                }catch(Throwable t){
                    arr = null;
                }
                if(arr != null && arr.length > 0){
                    out.append(arr[0]);
                }
            }else if(Character.isLetterOrDigit(c)){
                out.append(Character.toLowerCase(c));
            }
        }
        return out.toString();
    }

    public static boolean isCjk(char c){
        // Common CJK ranges used by Mindustry Chinese translations.
        return (c >= 0x4E00 && c <= 0x9FFF) || (c >= 0x3400 && c <= 0x4DBF);
    }

    /**
     * Fuzzy rules are intentionally simple and symmetric by using the same normalization
     * for both query and target pinyin.
     */
    public static String applyFuzzy(String s){
        if(s == null || s.isEmpty()) return "";

        // Order matters: handle multi-letter consonants first.
        String out = s;
        out = out.replace("zh", "z");
        out = out.replace("ch", "c");
        out = out.replace("sh", "s");

        // Common finals.
        out = out.replace("iang", "ian");
        out = out.replace("uang", "uan");
        out = out.replace("ang", "an");
        out = out.replace("eng", "en");
        out = out.replace("ing", "in");

        return out;
    }
}
