package com.avast.server.hdfsshell.ui;

import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Naive bash prompt interpreter into Java.
 * The implementation is thread safe.
 * <pre>
 * Sequence	Description
 * \a	An ASCII bell character (07)
 * \d	The date in "Weekday Month Date" format (e.g., "Tue May 26")
 * \e	An ASCII escape character (033)
 * \h	The hostname up to the first .
 * \H	The hostname (FQDN)
 * \j	The number of jobs currently managed by the shell
 * \l	The basename of the shell’s terminal device name
 * \n	Newline
 * \r	Carriage return
 * \s	The name of the shell, the basename of $0 (the portion following the final slash)
 * \t	The current time in 24-hour HH:MM:SS format
 * \T	The current time in 12-hour HH:MM:SS format
 * \@	The current time in 12-hour am/pm format
 * \A	The current time in 24-hour HH:MM format
 * \\u	The username of the current user
 * \v	The version of bash (e.g., 2.00)
 * \V 	The release of bash, version + patch level (e.g., 2.00.0)
 * \w	The current working directory, with $HOME abbreviated with a tilde  (note: tilde is not supported yet in HDFS)
 * \W	The basename of the current working directory, with $HOME abbreviated with a tilde (note: tilde is not supported yet in HDFS)
 * \!	The history number of this command
 * \#	The command number of this command
 * \$	If the effective UID is 0, a #, otherwise a $
 * \nnn	The character corresponding to the octal number nnn
 * \\	A backslash
 * \[	Begin a sequence of non-printing characters, which could be used to embed a terminal control sequence into the prompt
 * \]	End a sequence of non-printing characters</pre>
 * </pre>
 *
 * @author Vitasek L.
 */

public class SimpleBashPromptInterpreter {

    private final DateTimeFormatter PATTERN_24_HH_mm;
    private final DateTimeFormatter PATTERN_24_HH_mm_ss;
    private final DateTimeFormatter PATTERN_12_hh_mm;
    private final DateTimeFormatter PATTERN_12_hh_mm_ss;
    private final DateTimeFormatter WEEKDAY_MONTH_DATE;

    private final static Pattern DYNAMIC_ENCODING = Pattern.compile("\\\\[u#!HhdtT@AWw$]");

    private final String promptPS1;
    private boolean addResetEnd;
    private boolean useCacheForNetInfo = true;

    private final static String RESET_POSTFIX = ";39m";

    private String reset = "\033[37;0" + RESET_POSTFIX;


    private Supplier<String> username;
    private Supplier<String> commandNum;
    private Supplier<String> cwdAbsolut;
    private Supplier<String> cwdShort;
    private Supplier<String> historyNumber;
    private Supplier<Boolean> isRoot;

    //for cache reasons in default methods
    private String inetAddress = null;
    private String fqdn = null;

    private Supplier<String> fqdnSupplier = this::getFQDN;
    private Supplier<String> inetAddressSupplier = this::getInetAddress;

    private SimpleBashPromptInterpreter(final String promptPS1, final Supplier<String> appName, final Supplier<String> appVersion, Locale locale, final boolean addResetEnd) {
        this.addResetEnd = addResetEnd;
        this.promptPS1 = replaceStaticProperties(promptPS1, appName, appVersion);
        if (locale == null) {
            locale = Locale.getDefault();
        }
        PATTERN_24_HH_mm = DateTimeFormatter.ofPattern("HH:mm", locale);
        PATTERN_24_HH_mm_ss = DateTimeFormatter.ofPattern("HH:mm:ss", locale);
        PATTERN_12_hh_mm = DateTimeFormatter.ofPattern("h:mm a", locale);
        PATTERN_12_hh_mm_ss = DateTimeFormatter.ofPattern("h:mm:ss a", locale);
        WEEKDAY_MONTH_DATE = DateTimeFormatter.ofPattern("E MMM dd", locale);
    }

    public String interpret() {

        LocalDateTime now = null;//we don't want to call get time when not needed
        final Matcher matcher = DYNAMIC_ENCODING.matcher(promptPS1);
        final StringBuffer resultString = new StringBuffer();
        while (matcher.find()) {
            final String group = matcher.group();
            switch (group) {
                case "\\$":
                    appendReplacement(matcher, resultString, isRoot == null ? "$" : isRoot.get() ? "#" : "$");
                    break;
                case "\\u":
                    appendReplacement(matcher, resultString, username);
                    break;
                case "\\w":
                    appendReplacement(matcher, resultString, cwdAbsolut);
                    break;
                case "\\W":
                    appendReplacement(matcher, resultString, cwdShort);
                    break;
                case "\\#":
                    appendReplacement(matcher, resultString, commandNum);
                    break;
                case "\\!":
                    appendReplacement(matcher, resultString, historyNumber);
                    break;
                case "\\h":
                    appendReplacement(matcher, resultString, getComputerName());
                    break;
                case "\\H":
                    appendReplacement(matcher, resultString, fqdnSupplier);
                    break;
                case "\\d":
                    now = getNow(now);
                    appendReplacement(matcher, resultString, now.format(WEEKDAY_MONTH_DATE));
                    break;
                case "\\t":
                    now = getNow(now);
                    appendReplacement(matcher, resultString, now.format(PATTERN_24_HH_mm_ss));
                    break;
                case "\\T":
                    now = getNow(now);
                    appendReplacement(matcher, resultString, now.format(PATTERN_12_hh_mm_ss));
                    break;
                case "\\@":
                    now = getNow(now);
                    appendReplacement(matcher, resultString, now.format(PATTERN_12_hh_mm));
                    break;
                case "\\A":
                    now = getNow(now);
                    appendReplacement(matcher, resultString, now.format(PATTERN_24_HH_mm));
                    break;
            }
        }
        matcher.appendTail(resultString);

        final String newPrompt = resultString.toString().replace("\\\\", "\\");//do it as last

        return appendReset(newPrompt);
    }

    private String appendReset(String newPrompt) {
        return newPrompt + ((addResetEnd && !newPrompt.endsWith(RESET_POSTFIX)) ? getReset() : "");
    }

    private LocalDateTime getNow(LocalDateTime now) {
        if (now == null) {
            now = LocalDateTime.now();
        }
        return now;
    }


    public String getPromptPS1() {
        return promptPS1;
    }

    void setAddResetEnd(boolean addResetEnd) {
        this.addResetEnd = addResetEnd;
    }

    String getReset() {
        return reset;
    }

    void setReset(String reset) {
        this.reset = reset;
    }


    void setFqdnSupplier(Supplier<String> fqdnSupplier) {
        this.fqdnSupplier = fqdnSupplier;
    }

    void setInetAddressSupplier(Supplier<String> inetAddressSupplier) {
        this.inetAddressSupplier = inetAddressSupplier;
    }

    boolean isUseCacheForNetInfo() {
        return useCacheForNetInfo;
    }

    void setUseCacheForNetInfo(boolean useCacheForNetInfo) {
        this.useCacheForNetInfo = useCacheForNetInfo;
    }


    private String replaceStaticProperties(final String promptPS1, Supplier<String> appName, Supplier<String> appVersion) {
        final String appVersionValue = appVersion == null ? "" : appVersion.get();
        return promptPS1.
                replace("\\[", "").
                replace("\\]", "").
                replace("\\\\$", "\\$"). //some generators produce \\$ , we convert it to \$
                replace("\\e", "\033").
                replace("\\s", appName == null ? "" : appName.get()).
                replace("\\a", "\u0007").
                replace("\\V", appVersionValue).
                replace("\\v", appVersionValue).
                replace("\\n", "\n").
                replace("\\r", "\r");
    }

    private void appendReplacement(Matcher matcher, StringBuffer resultString, Supplier<String> supplier) {
        if (supplier == null) {
            supplier = () -> "";
        }
        appendReplacement(matcher, resultString, supplier.get());
    }

    private void appendReplacement(Matcher matcher, StringBuffer resultString, String value) {
        if (StringUtils.isEmpty(value)) {
            matcher.appendReplacement(resultString, "");
        } else {
            matcher.appendReplacement(resultString, Matcher.quoteReplacement(value));
        }
    }

    private String getFQDN() {
        if (useCacheForNetInfo && fqdn != null) {
            return fqdn;
        }
        try {
            return fqdn = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException ex) {
            return "unknownHost";
        }
    }


    private String getComputerName() {
        Map<String, String> env = System.getenv();
        if (env.containsKey("COMPUTERNAME")) {
            return env.get("COMPUTERNAME");
        } else {
            return env.getOrDefault("HOSTNAME", inetAddressSupplier == null ? "" : inetAddressSupplier.get());
        }
    }

    private String getInetAddress() {
        if (useCacheForNetInfo && inetAddress != null) {
            return inetAddress;
        }
        try {
            return inetAddress = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            return "unknownHost";
        }
    }


    @SuppressWarnings("unused")
    public static class Builder {

        private String promptPS1;
        private Locale locale = Locale.ENGLISH;

        private boolean addResetEnd = true;
        private boolean useCacheForNetInfo = true;

        private Supplier<String> fqdnSupplier;
        private Supplier<String> inetAddressSupplier;
        private Supplier<String> username;
        private Supplier<String> commandNum;
        private Supplier<String> cwdAbsolut;
        private Supplier<String> cwdShort;
        private Supplier<String> historyNumber;
        private Supplier<Boolean> isRoot;

        private Supplier<String> appName;
        private Supplier<String> appVersion;
        private String reset;


        public Builder(final String promptPS1) {
            this.promptPS1 = promptPS1;
        }


        public Builder setLocale(Locale locale) {
            this.locale = locale;
            return this;
        }


        public Builder setPromptPS1(String promptPS1) {
            this.promptPS1 = promptPS1;
            return this;
        }

        public Builder setFqdnSupplier(Supplier<String> fqdnSupplier) {
            this.fqdnSupplier = fqdnSupplier;
            return this;
        }

        public Builder setInetAddressSupplier(Supplier<String> inetAddressSupplier) {
            this.inetAddressSupplier = inetAddressSupplier;
            return this;
        }

        public Builder setUsername(Supplier<String> username) {
            this.username = username;
            return this;
        }

        public Builder setCommandNum(Supplier<String> commandNum) {
            this.commandNum = commandNum;
            return this;
        }

        public Builder setCwdAbsolut(Supplier<String> cwdAbsolut) {
            this.cwdAbsolut = cwdAbsolut;
            return this;
        }

        public Builder setCwdShort(Supplier<String> cwdShort) {
            this.cwdShort = cwdShort;
            return this;
        }

        public Builder setHistoryNumber(Supplier<String> historyNumber) {
            this.historyNumber = historyNumber;
            return this;
        }

        public Builder setIsRoot(Supplier<Boolean> isRoot) {
            this.isRoot = isRoot;
            return this;
        }

        public Builder setAppName(Supplier<String> appName) {
            this.appName = appName;
            return this;
        }

        public Builder setAppVersion(Supplier<String> appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        public Builder setAddResetEnd(boolean addResetEnd) {
            this.addResetEnd = addResetEnd;
            return this;
        }

        public Builder setReset(String reset) {
            this.reset = reset;
            return this;
        }

        public SimpleBashPromptInterpreter build() {
            final SimpleBashPromptInterpreter interpreter = new SimpleBashPromptInterpreter(promptPS1, appName, appVersion, locale, addResetEnd);
            interpreter.setUsername(username);

            interpreter.setIsRoot(isRoot);
            interpreter.setCommandNum(commandNum);
            interpreter.setUseCacheForNetInfo(useCacheForNetInfo);
            if (fqdnSupplier != null) {
                interpreter.setFqdnSupplier(fqdnSupplier);
            }
            if (historyNumber != null) {
                interpreter.setHistoryNumber(historyNumber);
            }
            if (inetAddressSupplier != null) {
                interpreter.setInetAddressSupplier(inetAddressSupplier);
            }
            if (cwdAbsolut != null) {
                interpreter.setCwdAbsolut(cwdAbsolut);
            }
            interpreter.setCwdShort(cwdShort);
            interpreter.setAddResetEnd(addResetEnd);
            if (reset != null) {
                interpreter.setReset(reset);
            }


            return interpreter;
        }

    }


    void setUsername(Supplier<String> username) {
        this.username = username;
    }

    void setCommandNum(Supplier<String> commandNum) {
        this.commandNum = commandNum;
    }

    void setCwdAbsolut(Supplier<String> cwdAbsolut) {
        this.cwdAbsolut = cwdAbsolut;
    }

    void setCwdShort(Supplier<String> cwdShort) {
        this.cwdShort = cwdShort;
    }

    void setHistoryNumber(Supplier<String> historyNumber) {
        this.historyNumber = historyNumber;
    }

    void setIsRoot(Supplier<Boolean> isRoot) {
        this.isRoot = isRoot;
    }
}
