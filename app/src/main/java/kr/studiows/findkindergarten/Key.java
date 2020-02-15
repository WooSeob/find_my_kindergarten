package kr.studiows.findkindergarten;

public final class Key {
    private final static String[][] Keys = {{"kindername","establish" , "addr"  , "telno"  , "opertime", "hpaddr"},
                                            {"crname"    ,"crtypename", "craddr", "crtelno", ""        , "crhome"}};

    private final static int NAME = 0;
    private final static int K_TYPE = 1;
    private final static int ADDR = 2;
    private final static int TELNO = 3;
    private final static int OPTIME = 4;
    private final static int HOMEPAGE = 5;

    public static String name(int Type){
        return Keys[Type][NAME];
    }
    public static String estType(int Type){return Keys[Type][K_TYPE];}
    public static String addr(int Type){
        return Keys[Type][ADDR];
    }
    public static String telno(int Type) {return Keys[Type][TELNO];}
    public static String optime(int Type) {return Keys[Type][OPTIME];}
    public static String homepage(int Type) {return Keys[Type][HOMEPAGE];}

}
