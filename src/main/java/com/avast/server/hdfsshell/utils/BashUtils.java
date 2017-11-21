package com.avast.server.hdfsshell.utils;

import org.springframework.util.StringUtils;

/**
 * @author Vitasek L.
 */
public class BashUtils {

    //private final static Pattern ARG_PATTERN = Pattern.compile("([^\"](\\S*)|\"(.+?)\")\\s*");

    private BashUtils() {
    }

    public static String[] parseArguments(final String input) {
        if (StringUtils.isEmpty(input)) {
            return new String[0];
        }
        return ArgumentTokenizer.tokenize(input).toArray(new String[0]);
//        final Matcher m = ARG_PATTERN.matcher(input);
//        final List<String> list = new ArrayList<>();
//        while (m.find()) {
//            final String group3 = m.group(3);
//            if (group3 == null) {
//                list.add(m.group(1));
//            } else {
//                list.add(group3);
//            }
//        }
//        return list.toArray(new String[list.size()]);
    }
}
