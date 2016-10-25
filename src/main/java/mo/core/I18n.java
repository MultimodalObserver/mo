package mo.core;

import es.eucm.i18n.I18N;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class I18n {

    private static final Logger logger = Logger.getLogger(I18n.class.getName());

    private ResourceBundle bundle;

    private I18N _i18n;

    private final static String BASE_NAME = "I18n";

    private final static List<String> baseNames = Arrays.asList(new String[]{BASE_NAME});

    public I18n(Class clazz) {
        Locale locale = Locale.getDefault();
        _i18n = new I18N();

        String packageName = clazz.getPackage().getName();

        ResourceBundle b1 = null;
        ResourceBundle b2 = null;
        try {
            b1 = ResourceBundle.getBundle("i18n." + packageName + ".I18n", locale);
        } catch (Exception ex) {
            //logger.log(Level.WARNING, null, ex);
        }

        try {
            b2 = ResourceBundle.getBundle(packageName + ".I18n", locale);
        } catch (Exception e) {
            //logger.log(Level.WARNING, null, e);
        }

        if (b1 == null && b2 != null) {
            bundle = b2;
        } else if (b1 != null && b2 == null) {
            bundle = b1;
        }

        if (bundle != null) {
            for (String k : bundle.keySet()) {
                _i18n.setMessage(k, bundle.getString(k));
            }
        } else {
            if (b1 != null && b2 != null) {
                int comp = compare(locale, b1.getLocale(), b2.getLocale());
                if (comp <= 0) {
                    for (String k : b1.keySet()) {
                        _i18n.setMessage(k, b1.getString(k));
                    }
                    for (String k : b2.keySet()) {
                        if (!_i18n.getMessages().containsKey(k)) {
                            _i18n.setMessage(k, b2.getString(k));
                        }
                    }
                } else if (comp > 0) {
                    for (String k : b2.keySet()) {
                        _i18n.setMessage(k, b2.getString(k));
                    }
                    for (String k : b1.keySet()) {
                        if (!_i18n.getMessages().containsKey(k)) {
                            _i18n.setMessage(k, b1.getString(k));
                        }
                    }
                }
            }
        }
    }

    // candidate1 > candidate2 -> returns negative
    // candidate1 < candidate2 -> returns positive
    private int compare(Locale goal, Locale candidate1, Locale candidate2) {
        String gLan = goal.getLanguage();
        String gCou = goal.getCountry();
        String gVar = goal.getVariant();

        String c1Lan = candidate1.getLanguage();
        String c1Cou = candidate1.getCountry();
        String c1Var = candidate1.getVariant();

        String c2Lan = candidate2.getLanguage();
        String c2Cou = candidate2.getCountry();
        String c2Var = candidate2.getVariant();

        if (gLan.equals(c1Lan) && gLan.equals(c2Lan)) {
            if (gCou.equals(c1Cou) && gCou.equals(c2Cou)) {
                if (gVar.equals(c1Var) && gVar.equals(c2Var)) {
                    return 0;
                } else if (gVar.equals(c1Var) && !gVar.equals(c2Var)) {
                    return -1;
                } else if (!gVar.equals(c1Var) && gVar.equals(c2Var)) {
                    return 1;
                } else if (c1Var.isEmpty() && c2Var.isEmpty()) {
                    return 0;
                } else if (c1Var.isEmpty()) {
                    return -1;
                } else if (c2Var.isEmpty()) {
                    return 1;
                } else {
                    return 0;
                }
            } else if (gCou.equals(c1Cou) && !gCou.equals(c2Cou)) {
                return -1;
            } else if (!gCou.equals(c1Cou) && gCou.equals(c2Cou)) {
                return 1;
            } else if (c1Cou.isEmpty() && c2Cou.isEmpty()) {
                return 0;
            } else if (c1Cou.isEmpty()) {
                return -1;
            } else if (c2Cou.isEmpty()) {
                return 1;
            } else {
                return 0;
            }
        } else if (gLan.equals(c1Lan) && !gLan.equals(c2Lan)) {
            return -1;
        } else if (!gLan.equals(c1Lan) && gLan.equals(c2Lan)) {
            return 1;
        } else if (c1Lan.isEmpty() && c2Lan.isEmpty()) {
            return 0;
        } else if (c1Lan.isEmpty()) {
            return -1;
        } else if (c2Lan.isEmpty()) {
            return 1;
        } else {
            return 0;
        }
    }

    public String s(String key) {
        return _i18n.m(key);
    }

    public String s(String key, Object... args) {
        return _i18n.m(key, args);
    }

    public String s(int cardinality, String keyOne, String keyMany) {
        return _i18n.m(cardinality, keyOne, keyMany);
    }

    public String s(int cardinality, String keyOne, String keyMany, Object... args) {
        return _i18n.m(cardinality, keyOne, keyMany, args);
    }

}
