package team12;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class phase3 {

    public static final String URL = "jdbc:postgresql://localhost:5432/knuMovie"; // URL
    public static final String USER = "knu"; // USER
    public static final String USER_PWD = "1234"; // PWD

    private static final Scanner scan = new Scanner(System.in); // Scanner for read input
    private static String email; // 로그인 이메일
    private static String pwd;
    private static int uid;

    public static void main(String argv[]) {
        Connection conn = null; // Connection 객체

        phase3 p = new phase3();

        try { // psql 드라이버 로드
            Class.forName("org.postgresql.Driver");
            p.p("Success!");
        } catch (ClassNotFoundException e) {
            System.err.println("error = " + e.getMessage());
            System.exit(1);
        }

        try { // psql db 연결
            conn = DriverManager.getConnection(URL, USER, USER_PWD);
            p.p("Connection success!");
        } catch (SQLException e) {
            System.err.println("Can't get a connection: " + e.getMessage());
            System.exit(1);
        }

        // 처음 접속 메뉴
        p.initialMenu(conn);
    }

    // ==========================메뉴===========================//

    // 시작 시 첫 메뉴
    public void initialMenu(Connection conn) {
        phase3.clearScreen();
        int input_integer = 0; // 입력된 정수
        int selection = 0;
        while (selection != 3) {
            p("KNU MOVIE");
            p("==================");
            p("Menu");
            p("==================");
            p("1. 로그인");
            p("2. 회원가입");
            p("3. 종료");

            input_integer = scan.nextInt();

            phase3.clearScreen();
            switch (input_integer) {
                case 1:
                    p("로그인");
                    p("==================");
                    signIn(conn);
                    break;
                case 2:
                    signUp(conn); // 2입력 시 회원가입
                    break;
                case 3:
                    return;
                default:
                    p("잘못된 입력입니다.");
                    break;
            }
        }
    }

    // 로그인 후 메인 메뉴
    private void mainMenu(Connection conn) {
        int selection = 0;
        while (selection != 3) {
            p("Knu Movie");
            p("==================");
            p("MENU");
            p("==================");
            p("1. 영화 검색");
            p("2. 평가 내역 확인");
            p("3. 회원 정보 수정");
            p("4. 로그아웃");
            p("5. 종료");
            p("6. 관리자 모드");
            selection = scan.nextInt();
            phase3.clearScreen();
            switch (selection) {
                case 1:
                    queryMovie(conn, false);
                    break;
                case 3:
                    changeAccountInfo(conn);
                    selection = 1;
                    break;
                case 4:
                    email = null;
                    pwd = null;
                    return;
                case 5:
                    System.exit(0);
                case 6:
                    adminMenu(conn);
                    break;
                case 2:
                    showRatingLog(conn);
            }
        }
    }

    // 관리자 메뉴
    private void adminMenu(Connection conn) {
        // 권한 확인
        int answer = 0;
        while (answer != 4) {
            phase3.clearScreen();
            String sql = "SELECT uid FROM ACCOUNT, ADMIN WHERE Email_add = '" + email + "' AND Account_id = uid";
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next() == false) {
                    p("권한이 없습니다.");
                    mainMenu(conn);
                }
            } catch (SQLException e) {
                p("error: " + e.getMessage());
            }

            p("관리자 메뉴");
            p("==================");
            p("1. 영상물 등록");
            p("2. 영상물 수정");
            p("3. 평가 내역 확인");
            p("4. 뒤로가기");
            answer = scan.nextInt();
            phase3.clearScreen();
            switch (answer) {
                case 1:
                    addMovie(conn);
                    break;
                case 2:
                    queryMovie(conn, true);
                    break;
                case 4:
                    return;
            }
        }
    }

    // 회원 정보 수정
    private void changeAccountInfo(Connection conn) {
        int selection = 1;
        while (selection != 9 && selection != 0) {
            try {
                String sql = "";
                sql = "SELECT * FROM ACCOUNT WHERE Email_add = '" + email + "'";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                rs.next();
                String fname = rs.getString(3);
                String lname = rs.getString(4);
                Date birthday_d = rs.getDate(5);
                String birthday = null;
                if (birthday_d != null)
                    birthday = birthday_d.toString();
                String sex = rs.getString(6);
                String password = rs.getString(7);
                String phone = rs.getString(8);
                String address = rs.getString(9);
                String job = rs.getString(10);
                int sid = rs.getInt(11);
                String membership = "basic";
                switch (sid) {
                    case 1:
                        membership = "basic";
                        break;
                    case 2:
                        membership = "premium";
                        break;
                    case 3:
                        membership = "prime";
                        break;
                }

                p("회원 정보 수정");
                p("==================");
                p("1. 이름");
                p("2. 생년월일");
                p("3. 성별");
                p("4. 비밀번호");
                p("5. 전화번호");
                p("6. 주소");
                p("7. 직업");
                p("8. 멤버쉽");
                p("9. 뒤로가기");
                p("0. 회원탈퇴");

                selection = scan.nextInt();
                phase3.clearScreen();
                switch (selection) {
                    case 1:
                        if (changeName(conn, lname, fname) == 0) {
                            changeName(conn, lname, fname);
                        } else {
                            phase3.clearScreen();
                            p("이름이 변경되었습니다.");
                        }
                        break;
                    case 2:
                        if (changeBirthday(conn, birthday) == 0) {
                            changeBirthday(conn, birthday);
                        } else {
                            phase3.clearScreen();
                            p("생년월일이 변경되었습니다.");
                        }
                        break;
                    case 3:
                        if (changeSex(conn, sex) == 0) {
                            changeSex(conn, sex);
                        } else {
                            phase3.clearScreen();
                            p("성별이 변경되었습니다.");
                        }
                        break;
                    case 4:
                        if (changePassword(conn, password) == 0)
                            changePassword(conn, password);
                        else {
                            phase3.clearScreen();
                            p("비밀번호가 변경되었습니다.");
                        }
                        break;
                    case 5:
                        if (changePhone(conn, phone) == 0) {
                            changePhone(conn, phone);
                        } else {
                            phase3.clearScreen();
                            p("전화번호가 변경되었습니다.");
                        }
                        break;
                    case 6:
                        if (changeAddress(conn, address) == 0)
                            changeAddress(conn, address);
                        else {
                            phase3.clearScreen();
                            p("주소가 변경되었습니다.");
                        }
                        break;
                    case 7:
                        if (changeJob(conn, job) == 0)
                            changeJob(conn, job);
                        else {
                            phase3.clearScreen();
                            p("직업이 변경되었습니다.");
                        }
                        break;
                    case 8:
                        if (changeMembership(conn, membership) == 0)
                            changeMembership(conn, membership);
                        else {
                            phase3.clearScreen();
                            p("멤버쉽이 변경되었습니다.");
                        }
                        break;
                    case 9:
                        return;
                    case 0:
                        if (withdraw(conn) != 0) {
                            phase3.clearScreen();
                            p("회원탈퇴가 완료되었습니다...");
                        }
                        break;
                }
            } catch (SQLException e) {
                p("error: " + e.getMessage());
            }
        }
    }

    private int selectGenre() {
        p("장르 선택");
        p("==================");
        p("1. Action");
        p("2. Adventure");
        p("3. Animation");
        p("4. Biography");
        p("5. Comedy");
        p("6. Crime");
        p("7. Drama");
        p("8. Family");
        p("9. Horror");
        p("10. Mystery");
        p("11. Romance");
        p("12. Sci-Fi");
        p("13. Thriller");
        p("14. Fantasy");
        p("0. 뒤로가기");
        int selection = scan.nextInt();
        phase3.clearScreen();
        if (selection == 0)
            return -1;
        else
            return selection;
    }

    // 영화 선택 후 메뉴
    private int afterSelectMovie(Connection conn, int mid, int uid, boolean isAdmin) {
        int res = 0;
        int selection = 0;
        boolean isError = false;

        while (res == 0) {
            isError = false;
            if (!isAdmin) {
                p("1. 평가하기");
            }
            if (isAdmin) {
                p("2. 수정하기");
            }
            p("0. 뒤로가기");
            selection = scan.nextInt();
            if (selection == 1) {
                p("평가 하실 점수를 입력해주세요. (0 ~ 10)");
                int rating = 0;
                try {
                    rating = scan.nextInt();
                } catch (InputMismatchException e) {
                    p("잘못 입력하셨습니다.");
                    scan.nextLine();
                    isError = true;
                }
                if (!isError) {
                    String sql = "INSERT INTO RATING (Single_rating, mid, uid) " + "VALUES (" + rating + ", " + mid
                            + ", " + uid + ")";
                    try {
                        Statement stmt = conn.createStatement();
                        res = stmt.executeUpdate(sql);
                        if (res == 1) {
                            phase3.clearScreen();
                            p("평가가 등록 되었습니다.");
                        }
                    } catch (SQLException e) {
                        p("잘못 입력하셨습니다.");
                    }
                }
            } else if (selection == 2) {
                phase3.clearScreen();
                updateMovie(conn, mid);
                return 9;
            } else {
                phase3.clearScreen();
                return selection;
            }
        }
        return selection;
    }

    // 평가 내역 확인 후 메뉴
    private int reRating(Connection conn, int mid, int uid) {
        int res = 0;
        int selection = 0;
        while (res == 0) {
            p("1. 평가 수정하기");
            p("0. 뒤로가기");
            selection = scan.nextInt();
            if (selection == 1) {
                p("평가 하실 점수를 입력해주세요.");
                int rating = scan.nextInt();
                String sql = "UPDATE RATING SET Single_rating = " + rating + " WHERE uid = " + uid + " AND mid = "
                        + mid;
                try {
                    Statement stmt = conn.createStatement();
                    res = stmt.executeUpdate(sql);
                    if (res == 1) {
                        phase3.clearScreen();
                        p("평가가 수정 되었습니다.");
                    } else {
                        phase3.clearScreen();
                        p("잘못 입력하셨습니다.");
                    }
                } catch (SQLException e) {
                    p("잘못 입력하셨습니다.");
                }
            } else {
                phase3.clearScreen();
                return selection;
            }
        }
        return selection;
    }
    // ================================functions===========================

    // 회원가입
    private void signUp(Connection conn) {
        int check = 2;
        while (check == 2) {
            p("이메일을 입력하세요.");
            String email = scan.next();
            if (!email.contains("@")) {
                p("이메일의 형식이 잘못되었습니다. 다시 입력 해주세요.");
                email = scan.next();
            }
            p("비밀번호를 입력하세요.");
            String password = scan.next();
            p("이름(first name)을 입력하세요.");
            String fname = scan.next();
            p("성(last name)을 입력하세요.");
            String lname = scan.next();

            p("=========================");
            p("이메일: " + email);
            p("비밀번호: " + password);
            p("이름: " + fname);
            p("성: " + lname);
            p("제대로 입력 됐나요?");
            p("1. 네");
            p("2. 아니요");
            p("3. 종료");
            check = scan.nextInt();
            if (check == 1) {
                try {
                    String sql = "INSERT INTO ACCOUNT (email_add, First_name, Last_name, Password, sid)"
                            + " VALUES (?, ?, ?, ?, 1)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, email);
                    ps.setString(2, fname);
                    ps.setString(3, lname);
                    ps.setString(4, password);
                    int res = ps.executeUpdate();
                    if (res == 1) {
                        p("가입완료!");
                    }
                } catch (SQLException e) {
                    System.err.println("잘못 입력되었습니다. 다시 한번 확인 해주세요.");
                    check = 2;
                }
            }
        }
    }

    // 로그인
    private void signIn(Connection conn) {
        p("email: ");
        email = scan.next();
        p("password: ");
        pwd = scan.next();
        String sql = "SELECT * FROM ACCOUNT WHERE Email_add = ? AND Password = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, pwd);
            ResultSet rs = ps.executeQuery();
            if (rs.next() == false) {
                p("이메일 또는 비밀번호를 다시 확인해주세요.");
                signIn(conn);
            } else {
                uid = rs.getInt(1);
                phase3.clearScreen();
                p("로그인 완료!");
                mainMenu(conn);
            }
        } catch (SQLException e) {
            p("error: " + e.getMessage());
        }
    }

    // p = print
    private void p(String message) {
        System.out.println(message);
    }

    private int changeName(Connection conn, String lname, String fname) {
        try {
            Statement stmt = conn.createStatement();
            p("현재 이름: " + lname + fname);
            p("바꾸실 이름을 입력해주세요.");
            p("성: ");
            lname = scan.next();
            p("이름: ");
            fname = scan.next();
            String sql = "UPDATE ACCOUNT SET First_name = '" + fname + "', " + "Last_name = '" + lname + "'"
                    + "WHERE Email_add = '" + email + "'";
            stmt.executeUpdate(sql);
            return 1;
        } catch (SQLException e) {
            p("이름 또는 성이 너무 길거나 짧습니다.");
            p("다시 입력 해주세요.");
            return 0;
        }
    }

    private int changeBirthday(Connection conn, String birthday) {
        try {
            Statement stmt = conn.createStatement();
            if (birthday != null)
                p("현재 생년월일: " + birthday);
            p("바꾸실 생년월일을 입력해주세요.");
            p("ex) yyyy-mm-dd");
            birthday = scan.next();
            String sql = "UPDATE ACCOUNT SET " + "Birthday = '" + birthday + "'" + "WHERE Email_add = '" + email + "'";
            stmt.executeUpdate(sql);
            return 1;
        } catch (SQLException e) {
            p("날짜 형식에 맞추어 다시 입력 해주세요.");
            return 0;
        }
    }

    private int changeSex(Connection conn, String sex) {
        try {
            Statement stmt = conn.createStatement();
            if (sex != null)
                p("성별: " + sex);
            p("바꾸실 성별을 입력해주세요.");
            p("1. 남성");
            p("2. 여성");
            p("* 주민등록 표기 상 성별");
            int sexnum = scan.nextInt();
            if (sexnum == 1)
                sex = "Male";
            else if (sexnum == 2)
                sex = "Female";
            else {
                p("잘못 입력하셨습니다. 다시 입력 해주세요");
                changeSex(conn, sex);
            }
            String sql = "UPDATE ACCOUNT SET " + "Sex = '" + sex + "'" + "WHERE Email_add = '" + email + "'";
            stmt.executeUpdate(sql);
            return 1;
        } catch (SQLException e) {
            p("잘못 입력하셨습니다. 다시 입력 해주세요");
            return 0;
        }
    }

    private int changePassword(Connection conn, String pwd) {
        try {
            Statement stmt = conn.createStatement();
            p("현재 비밀번호: " + pwd);
            p("바꾸실 비밀번호를 입력해주세요.");
            pwd = scan.next();
            String sql = "UPDATE ACCOUNT SET " + "Password = '" + pwd + "'" + "WHERE Email_add = '" + email + "'";
            stmt.executeUpdate(sql);
            return 1;
        } catch (SQLException e) {
            p("잘못 입력하셨습니다. 다시 입력 해주세요");
            return 0;
        }
    }

    private int changePhone(Connection conn, String phone) {
        try {
            Statement stmt = conn.createStatement();
            p("현재 전화번호: " + phone);
            p("바꾸실 전화번호를 입력해주세요.");
            p("ex) 010-xxxx-xxxx");
            String newPhone = scan.next();
            boolean tel_check = Pattern.matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", newPhone);
            if (!tel_check) {
                p("전화번호 형식에 맞지 않습니다. 다시 입력해주세요.");
                changePhone(conn, phone);
            }
            String sql = "UPDATE ACCOUNT SET " + "Phone = '" + newPhone + "'" + "WHERE Email_add = '" + email + "'";
            stmt.executeUpdate(sql);
            return 1;
        } catch (SQLException e) {
            p("잘못 입력하셨습니다. 다시 입력 해주세요");
            return 0;
        }
    }

    private int changeAddress(Connection conn, String address) {
        try {
            Statement stmt = conn.createStatement();
            if (address != null)
                p("현재 주소: " + address);
            p("바꾸실 주소를 입력해주세요.");
            scan.nextLine();
            address = scan.nextLine();
            String sql = "UPDATE ACCOUNT SET " + "Address = '" + address + "'" + "WHERE Email_add = '" + email + "'";
            stmt.executeUpdate(sql);
            return 1;
        } catch (SQLException e) {
            p("잘못 입력하셨습니다. 다시 입력 해주세요");
            return 0;
        }
    }

    private int changeJob(Connection conn, String job) {
        try {
            Statement stmt = conn.createStatement();
            if (job != null)
                p("현재 직업: " + job);
            p("바꾸실 직업 입력해주세요.");
            job = scan.next();
            String sql = "UPDATE ACCOUNT SET " + "Job = '" + job + "'" + "WHERE Email_add = '" + email + "'";
            stmt.executeUpdate(sql);
            return 1;
        } catch (SQLException e) {
            p("잘못 입력하셨습니다. 다시 입력 해주세요");
            return 0;
        }
    }

    private int changeMembership(Connection conn, String membership) {
        try {
            Statement stmt = conn.createStatement();
            p("현재 멤버쉽: " + membership);
            p("바꾸실 멤버쉽을 입력해주세요.");
            p("1. basic");
            p("2. premium");
            p("3. prime");
            int sid = scan.nextInt();
            String sql = "UPDATE ACCOUNT SET " + "sid = '" + sid + "'" + "WHERE Email_add = '" + email + "'";
            stmt.executeUpdate(sql);
            return 1;
        } catch (SQLException e) {
            p("잘못 입력하셨습니다. 다시 입력 해주세요");
            return 0;
        }
    }

    private int withdraw(Connection conn) {
        try {
            phase3.clearScreen();
            Statement stmt = conn.createStatement();
            p("정말로 회원탈퇴를 하시겠습니까??");
            p("1. 네");
            p("2. 아니요.");
            int answer = scan.nextInt();
            if (answer == 1) {
                phase3.clearScreen();
                p("진짜요??");
                p("1. 네;;");
                p("2. 아니요.");
                answer = scan.nextInt();
            }
            if (answer == 1) {
                String sql = "DELETE FROM ACCOUNT " + "WHERE Email_add = '" + email + "'";
                stmt.executeUpdate(sql);
                return 1;
            } else
                return 0;
        } catch (SQLException e) {
            p("잘못 입력하셨습니다. 다시 입력 해주세요");
            return 0;
        }
    }

    // 영화 추가
    private void addMovie(Connection conn) {
        p("영상물 추가");
        p("===========================");
        p("Original title을 입력하세요.");
        scan.nextLine();
        String originalTitle = scan.nextLine();
        p("");
        p("type을 입력하세요.");
        p("1. Movie");
        p("2. Tv Series");
        p("3. Knu Origianl");
        int typeNum = scan.nextInt();
        String type = "";
        switch (typeNum) {
            case 1:
                type = "movie";
            case 2:
                type = "tvSeries";
            case 3:
                type = "knuOriginal";
        }
        p("");
        p("청소년 관람불가 여부를 입력하세요.");
        p("1. 청소년 관람 불가");
        p("0. 청소년 관람 가능");
        int isAdultNum = scan.nextInt();
        boolean isAdult = (isAdultNum == 1);

        try {
            String sql = "INSERT INTO MOVIE (" + "Original_title, Type, Is_adult) VALUES (" + "'" + originalTitle
                    + "', '" + type + "', " + isAdult + ")";
            Statement stmt = conn.createStatement();
            int res = stmt.executeUpdate(sql);
            if (res == 1) {
                phase3.clearScreen();
                p("영화가 등록되었습니다");
                try {
                    System.in.read();
                } catch (IOException e) {

                }
                return;
            } else {
                p("잘못 입력하셨습니다. 다시 입력 해주세요.");
                try {
                    System.in.read();
                } catch (IOException e) {

                }
                return;
            }
        } catch (SQLException e) {
            p("error: " + e.getMessage());
        }
    }

    // 영상물 수정
    private void updateMovie(Connection conn, int mid) {
        String sql = "";
        int selection = -1;
        p("영상물 수정");
        p("=================");
        p("수정 하실 내용을 선택해주세요.");
        p("");
        p("1. 장르");
        p("2. 유형");
        p("3. 개봉 (방영 시작) 날짜");
        p("4. 종영 날짜");
        p("5. 청소년 관람 가능 여부");
        /* p("6. 배우"); */
        p("0. 뒤로가기");
        selection = scan.nextInt();
        phase3.clearScreen();
        switch (selection) {
            case 1:
                updateGenre(conn, mid);
                break;
            case 2:
                while (selection != 0) {
                    try {
                        selection = -1;
                        sql = "SELECT Type FROM MOVIE WHERE Movie_id = " + mid;
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(sql);
                        rs.next();
                        p("==================");
                        p("현재 유형: " + rs.getString(1));
                        p("");
                        p("바꾸실 유형을 선택하세요.");
                        p("1. Movie");
                        p("2. TvSeries");
                        p("3. KnuMovie");
                        p("0. 뒤로가기");
                        selection = scan.nextInt();
                        phase3.clearScreen();
                        switch (selection) {
                            case 1:
                                sql = "UPDATE MOVIE SET Type = 'Movie' WHERE Movie_id = " + mid;
                                stmt.executeUpdate(sql);
                                p("수정 완료");
                                break;
                            case 2:
                                sql = "UPDATE MOVIE SET Type = 'tvSeries' WHERE Movie_id = " + mid;
                                stmt.executeUpdate(sql);
                                p("수정 완료");
                                break;
                            case 3:
                                sql = "UPDATE MOVIE SET Type = 'knuMovie' WHERE Movie_id = " + mid;
                                stmt.executeUpdate(sql);
                                p("수정 완료");
                                break;
                            case 0:
                                return;
                        }
                    } catch (SQLException e) {
                        p("error: " + e.getMessage());
                    }
                }
                break;
            case 3:
                while (selection != 0) {
                    try {
                        selection = -1;
                        sql = "SELECT Start_year FROM MOVIE WHERE Movie_id = " + mid;
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(sql);
                        rs.next();
                        p("==================");
                        p("현재 개봉 (방영 시작) 날짜: " + rs.getString(1));
                        p("==================");
                        p("1. 날짜 수정");
                        p("0. 뒤로가기");
                        p("===================");
                        selection = scan.nextInt();
                        switch (selection) {
                            case 1:
                                String year = "";
                                String pattern = "";
                                boolean isValid = false;
                                while (!isValid) {
                                    p("바꿀 날짜를 입력해주세요. (ex)1984-01-31");
                                    year = scan.next();
                                    pattern = "^((19|20)\\d\\d)?([- /.])?(0[1-9]|1[012])([- /.])?(0[1-9]|[12][0-9]|3[01])$";
                                    isValid = Pattern.matches(pattern, year);
                                    if (!isValid)
                                        p("날짜 형식이 잘못 되었습니다. 다시 입력해 주세요.");
                                }
                                sql = "UPDATE MOVIE SET Start_year = '" + year + "' WHERE Movie_id = " + mid;
                                stmt.executeUpdate(sql);
                                phase3.clearScreen();
                                p("수정 완료");
                                break;
                            case 0:
                                phase3.clearScreen();
                                return;
                        }
                    } catch (SQLException e) {
                        p("error: " + e.getMessage());
                    }
                }
                break;
            case 4:
                while (selection != 0) {
                    try {
                        selection = -1;
                        sql = "SELECT End_year FROM MOVIE WHERE Movie_id = " + mid;
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(sql);
                        rs.next();
                        p("==================");
                        p("현재 종영 날짜: " + rs.getString(1));
                        p("==================");
                        p("1. 날짜 수정");
                        p("0. 뒤로가기");
                        p("===================");
                        selection = scan.nextInt();
                        phase3.clearScreen();
                        switch (selection) {
                            case 1:
                                String year = "";
                                String pattern = "";
                                boolean isValid = false;
                                while (!isValid) {
                                    p("바꿀 날자를 입력해주세요. (ex)1984-01-31");
                                    year = scan.next();
                                    pattern = "^((19|20)\\d\\d)?([- /.])?(0[1-9]|1[012])([- /.])?(0[1-9]|[12][0-9]|3[01])$";
                                    isValid = Pattern.matches(pattern, year);
                                    if (!isValid)
                                        p("날짜 형식이 잘못 되었습니다. 다시 입력해 주세요.");
                                }
                                sql = "UPDATE MOVIE SET End_year = '" + year + "' WHERE Movie_id = " + mid;
                                stmt.executeUpdate(sql);
                                p("수정 완료");
                                break;
                            case 0:
                                return;
                        }
                    } catch (SQLException e) {
                        p("error: " + e.getMessage());
                    }
                }
                break;
            case 5:
                while (selection != 0) {
                    try {
                        selection = -1;
                        sql = "SELECT Is_adult FROM MOVIE WHERE Movie_id = " + mid;
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(sql);
                        rs.next();
                        p("==================");
                        if (rs.getBoolean(1)) {
                            p("현재 등급: 청소년 관람 불가");
                        } else
                            p("현재 등급: 청소년 관람 가능");
                        p("");
                        p("바꿀 등급을 선택해주세요.");
                        p("1. 청소년 관람 불가");
                        p("2. 청소년 관람 가능");
                        p("0. 뒤로가기");
                        selection = scan.nextInt();
                        phase3.clearScreen();
                        switch (selection) {
                            case 1:
                                sql = "UPDATE MOVIE SET Is_adult = true WHERE Movie_id = " + mid;
                                stmt.executeUpdate(sql);
                                p("수정 완료");
                                break;
                            case 2:
                                sql = "UPDATE MOVIE SET Is_adult = false WHERE Movie_id = " + mid;
                                stmt.executeUpdate(sql);
                                p("수정 완료");
                                break;
                            case 0:
                                return;
                        }
                    } catch (SQLException e) {
                        p("error: " + e.getMessage());
                    }
                }
                break;
            /*
             * case 6: break;
             */
            case 0:
                return;
        }

    }

    // 영화 검색
    private void queryMovie(Connection conn, boolean isAdmin) {
        String type, startYear, endYear, region, actor;
        int runningTime, genreId, selection = -1;
        double ratingMin, ratingMax;
        type = startYear = endYear = region = actor = "";
        runningTime = genreId = -1;
        ratingMin = ratingMax = -1;

        ArrayList<MovieData> movieData = new ArrayList<MovieData>();

        while (selection != 0) {
            p("영상물 검색");
            p("==========================");
            p("1. 제목 입력 및 검색");
            p("2. 장르 선택");
            p("3. 유형 선택");
            p("4. 개봉 국가 선택");
            p("5. 평가 점수 선택");
            p("6. 개봉(방영 시작)년도 선택");
            p("7. 방영 종료 년도 선택");
            p("8. 배우 선택");
            p("0. 뒤로가기");
            selection = scan.nextInt();
            phase3.clearScreen();
            switch (selection) {
                case 2:
                    genreId = selectGenre();
                    break;
                case 3:
                    p("유형 선택");
                    p("===================");
                    p("1. Movie");
                    p("2. TvSeries");
                    p("3. Knu Original");
                    p("4. 뒤로가기");
                    selection = scan.nextInt();
                    phase3.clearScreen();
                    switch (selection) {
                        case 1:
                            type = "movie";
                            break;
                        case 2:
                            type = "tvSeries";
                            break;
                        case 3:
                            type = "knuOriginal";
                            break;
                        case 4:
                            break;
                    }
                    break;
                case 4:
                    p("개봉 국가 선택");
                    p("=======================================");
                    p("개봉 국가를 입력 하세요 (ex) KR, JP, US...");
                    region = scan.next();
                    phase3.clearScreen();
                    break;
                case 5:
                    enterRating(ratingMin, ratingMax);
                    phase3.clearScreen();
                    break;
                case 6:
                    startYear = enterYear("개봉일 또는 방영 시작일");
                    phase3.clearScreen();
                    break;
                case 7:
                    endYear = enterYear("방영 종료일");
                    phase3.clearScreen();
                    break;
                case 8:
                    p("영화 배우 선택");
                    p("==============================");
                    p("찾으실 영화 배우를 입력하세요");
                    scan.nextLine();
                    actor = scan.nextLine();
                    phase3.clearScreen();
                    break;
                case 0:
                    return;
                case 1:
                    p("영화 제목을 입력 해주세요. (조건만 검색 시 빈칸):");
                    scan.nextLine();
                    String title = scan.nextLine();
                    phase3.clearScreen();
                    String sql = "SELECT DISTINCT Original_title, DATE_PART('year', Start_year) AS Year, Movie_id FROM MOVIE, GENRE_OF"
                            + ", ACTOR, ACTOR_OF, VERSION"
                            + " WHERE Movie_id = GENRE_OF.mid AND Actor_id = aid AND ACTOR_OF.mid = Movie_id AND VERSION.mid = Movie_id"
                            + " AND" + " Original_title LIKE '%" + title + "%' ";
                    if (!isAdmin) {
                        sql = sql.concat("AND NOT EXISTS (SELECT * FROM RATING WHERE uid = " + uid
                                + " AND Movie_id = RATING.mid)");
                    }
                    if (genreId != -1)
                        sql = sql.concat(" AND gen = " + genreId);
                    if (type != "")
                        sql = sql.concat(" AND Type = '" + type + "'");
                    if (region != "")
                        sql = sql.concat(" AND Region = '" + region + "'");
                    if (actor != "")
                        sql = sql.concat(" AND Actor_name = '" + actor + "'");
                    if (runningTime != -1)
                        sql = sql.concat(" AND Running_time = " + runningTime);
                    if (ratingMin != -1 && ratingMax != -1)
                        sql = sql.concat(" AND rating >= " + ratingMin + " AND rating <= " + ratingMax);
                    if (startYear != "")
                        sql = sql.concat(" AND DATE_PART('year',Start_year) = '" + startYear + "'");
                    if (endYear != "")
                        sql = sql.concat(" AND DATE_PART('year',End_year) = '" + endYear + "'");

                    try {
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(sql);
                        int i = 0;
                        while (rs.next()) {
                            MovieData newMovieData = new MovieData(i, rs.getString(1), rs.getString(2), rs.getInt(3));
                            movieData.add(i, newMovieData);
                            i++;
                        }
                        searchResult(movieData, conn, isAdmin);
                    } catch (SQLException e) {
                        p("error: " + e.getMessage());
                    }
                    type = startYear = endYear = region = actor = "";
                    runningTime = genreId = -1;
                    ratingMin = ratingMax = -1;

                    break;
            }
        }
    }

    private void searchResult(ArrayList<MovieData> movieData, Connection conn, boolean isAdmin) {

        p("검색 결과");
        p("=================================");
        int i = 0;
        int selection = 1;
        while (selection != 0) {
            if (selection == 1) {
                for (int j = 1; j <= 10 && i < movieData.size(); j++) {
                    p(i + 1 + ". " + movieData.get(i).title + " (" + movieData.get(i).year + ")");
                    i++;
                }
            } else if (selection == 2) {
                i -= 11;
                i /= 10;
                i *= 10;
                for (int j = 1; j <= 10; j++) {
                    p(i + 1 + ". " + movieData.get(i).title + " (" + movieData.get(i).year + ")");
                    i++;
                }
            } else {
                p("영화를 선택 해 주세요.");
                selection = scan.nextInt();
                phase3.clearScreen();
                try {
                    while (selection != 0) {
                        Statement stmt = conn.createStatement();
                        String sql = "SELECT Original_title, rating, Running_time, Start_year, End_year, Type, Is_adult, Genre "
                                + "FROM MOVIE, GENRE, GENRE_OF WHERE Movie_id = mid AND Genre_id = gen AND "
                                + "Movie_id = " + movieData.get(selection - 1).movieId;
                        ResultSet rs = stmt.executeQuery(sql);
                        rs.next();
                        p("");
                        p("영상 제목: " + rs.getString(1));
                        p("평점: " + rs.getDouble(2));
                        p("러닝타임: " + rs.getInt(3));
                        p("개봉(방영시작)일: " + rs.getDate(4));
                        if (rs.getDate(5) != null)
                            p("방영 종료일: " + rs.getDate(5));
                        p("영상 종류: " + rs.getString(6));
                        if (rs.getBoolean(7))
                            p("청소년 관람 불가");
                        else
                            p("청소년 관람 가능");
                        System.out.print("장르: " + rs.getString(8));
                        while (rs.next()) {
                            System.out.print(", " + rs.getString(8));
                        }
                        sql = "SELECT Actor_name FROM ACTOR, ACTOR_OF, MOVIE "
                                + "WHERE Movie_id = mid AND Actor_id = aid AND " + "Movie_id = "
                                + movieData.get(selection - 1).movieId;
                        rs = stmt.executeQuery(sql);
                        rs.next();
                        p("");
                        System.out.print("배우: " + rs.getString(1));
                        while (rs.next())
                            System.out.print(", " + rs.getString(1));
                        p("");
                        p("");
                        int insideSelection = afterSelectMovie(conn, movieData.get(selection - 1).movieId, uid,
                                isAdmin);
                        if (insideSelection == 0)
                            return;
                    }
                } catch (SQLException e) {
                }
            }
            p("=================================");
            if (i < movieData.size())
                p("1. 다음");
            int j = i - 1;
            j /= 10;
            if (j > 0)
                p("2. 이전");
            p("3. 선택");
            p("0. 뒤로가기");
            selection = scan.nextInt();
            if (selection != 3)
                phase3.clearScreen();
        }
        return;
    }

    private String enterYear(String startOrEnd) {
        p(startOrEnd + " 선택");
        p("=======================================");
        boolean isValid = false;
        String year = "";
        while (!isValid) {
            p(startOrEnd + "을 입력해주세요 (ex) 1984): ");
            year = scan.next();
            String pattern = "^\\d{4}$";
            isValid = Pattern.matches(pattern, year);
            if (!isValid)
                p("날짜 형식이 잘못 되었습니다. 다시 입력해 주세요.");
        }
        return year;
    }

    private void enterRating(Double ratingMin, Double ratingMax) {
        ratingMin = 0.0;
        ratingMax = 0.0;
        while (ratingMin != -1.0 && ratingMax != -1.0) {
            p("평가 점수 선택");
            p("=======================================");
            p("(0 입력 시 뒤로가기)");
            p("최소 점수를 입력하세요 (0.0 ~ 10.0): ");
            try {
                ratingMin = scan.nextDouble();
            } catch (InputMismatchException e) {
                p("잘못 입력하셨습니다. 다시 입력해주세요.");
                ratingMin = -1.0;
            }
            if (ratingMin == 0.0)
                return;
            p("최대 점수를 입하세요 (0.0 ~ 10.0): ");
            try {
                ratingMax = scan.nextDouble();
            } catch (InputMismatchException e) {
                p("잘못 입력하셨습니다. 다시 입력해주세요.");
                ratingMax = -1.0;
            }
            if (ratingMax == 0.0)
                return;

            if (ratingMin > ratingMax) {
                p("잘못 입력하셨습니다. 다시 입력해주세요.");
                ratingMax = ratingMin = -1.0;
            }
        }
    }

    // 평가 내역 확인
    private void showRatingLog(Connection conn) {
        ArrayList<MovieData> movieData = new ArrayList<MovieData>();
        int selection = 1;
        int i = 0;
        String sql = "SELECT Original_title, DATE_PART('year', Start_year), Movie_id, Single_rating FROM RATING, MOVIE "
                + "WHERE uid = " + uid + " AND mid = Movie_id";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            i = 1;
            while (rs.next()) {
                MovieData movie = new MovieData(i++, rs.getString(1), rs.getString(2), rs.getInt(3));
                movie.setRating(rs.getInt(4));
                movieData.add(movie);
            }
        } catch (SQLException e) {

        }
        i = 0;
        p("===============================================");
        while (selection != 0) {
            if (selection == 1) {
                for (int j = 1; j <= 10 && i < movieData.size(); j++) {
                    p(i + 1 + ". " + movieData.get(i).title + " (" + movieData.get(i).year + ")");
                    i++;
                }
            } else if (selection == 2) {
                i -= 11;
                i /= 10;
                i *= 10;
                for (int j = 1; j <= 10; j++) {
                    p(i + 1 + ". " + movieData.get(i).title + " (" + movieData.get(i).year + ")");
                    i++;
                }
            } else {
                p("영화를 선택해주세요");
                selection = scan.nextInt();
                phase3.clearScreen();
                while (selection != 0) {
                    try {
                        Statement stmt = conn.createStatement();
                        sql = "SELECT Original_title, rating, Running_time, Start_year, End_year, Type, Is_adult, Genre "
                                + "FROM MOVIE, GENRE, GENRE_OF WHERE Movie_id = mid AND Genre_id = gen AND "
                                + "Movie_id = " + movieData.get(selection - 1).movieId;
                        ResultSet rs = stmt.executeQuery(sql);
                        rs.next();
                        p("");
                        p("영상 제목: " + rs.getString(1));
                        p("평점: " + rs.getDouble(2));
                        p("러닝타임: " + rs.getInt(3));
                        p("개봉(방영시작)일: " + rs.getDate(4));
                        if (rs.getDate(5) != null)
                            p("방영 종료일: " + rs.getDate(5));
                        p("영상 종류: " + rs.getString(6));
                        if (rs.getBoolean(7))
                            p("청소년 관람 불가");
                        else
                            p("청소년 관람 가능");
                        System.out.print("장르: " + rs.getString(8));
                        while (rs.next()) {
                            System.out.print(", " + rs.getString(8));
                        }
                        sql = "SELECT Actor_name FROM ACTOR, ACTOR_OF, MOVIE "
                                + "WHERE Movie_id = mid AND Actor_id = aid AND " + "Movie_id = "
                                + movieData.get(selection - 1).movieId;
                        rs = stmt.executeQuery(sql);
                        rs.next();
                        p("");
                        System.out.print("배우: " + rs.getString(1));
                        while (rs.next())
                            System.out.print(", " + rs.getString(1));
                        p("");
                        p("");
                        int insideSelection = reRating(conn, movieData.get(selection - 1).movieId, uid);
                        if (insideSelection == 0)
                            return;
                    } catch (SQLException e) {

                    }
                }
            }

            p("=================================");
            if (i < movieData.size())
                p("1. 다음");
            int j = i - 1;
            j /= 10;
            j *= 10;
            if (j > 0)
                p("2. 이전");
            p("3. 선택");
            p("0. 뒤로가기");
            selection = scan.nextInt();
        }
    }

    private void updateGenre(Connection conn, int mid) {
        ArrayList<String> genre = new ArrayList<String>();
        ArrayList<Integer> genreId = new ArrayList<Integer>();
        String sql = "";
        int selection = 1, gen = 0, i = 0;
        while (selection != 0) {
            try {
                Statement stmt = conn.createStatement();
                sql = "SELECT Genre, gen FROM GENRE, GENRE_OF WHERE mid = " + mid + " AND Genre_id = gen";
                ResultSet rs = stmt.executeQuery(sql);
                genre.clear();
                genreId.clear();
                while (rs.next()) {
                    genre.add(i, rs.getString(1));
                    genreId.add(i, rs.getInt(2));
                }
                for (int j = 0; j < genre.size(); j++) {
                    p((j + 1) + ". " + genre.get(j));
                }
            } catch (SQLException e) {
                p("error: " + e.getMessage());
            }
            p("=============");
            p("1. 추가");
            p("2. 삭제");
            p("0. 뒤로가기");
            selection = scan.nextInt();
            phase3.clearScreen();

            switch (selection) {
                case 1:
                    selection = 9;
                    gen = selectGenre();
                    sql = "INSERT INTO GENRE_OF(mid,gen) VALUES(" + mid + ", " + gen + ")";
                    try {
                        Statement stmt = conn.createStatement();
                        int res = stmt.executeUpdate(sql);
                        if (res == 1) {
                            phase3.clearScreen();
                            p("장르가 추가되었습니다.");
                            phase3.pause();
                        }
                    } catch (SQLException e) {
                        p("error: " + e.getMessage());
                    }
                    break;
                case 2:
                    try {
                        Statement stmt = conn.createStatement();
                        p("삭제할 장르를 선택해주세요.");
                        p("=======================");
                        sql = "SELECT Genre, gen FROM GENRE, GENRE_OF WHERE mid = " + mid + " AND Genre_id = gen";
                        ResultSet rs = stmt.executeQuery(sql);
                        genre.clear();
                        genreId.clear();
                        while (rs.next()) {
                            genre.add(i, rs.getString(1));
                            genreId.add(i, rs.getInt(2));
                        }
                        for (int j = 0; j < genre.size(); j++) {
                            p((j + 1) + ". " + genre.get(j));
                        }
                        p("========================");
                        gen = scan.nextInt();
                        sql = "DELETE FROM GENRE_OF WHERE mid = " + mid + " AND gen = " + genreId.get(gen - 1);

                        int res = stmt.executeUpdate(sql);
                        if (res == 1) {
                            phase3.clearScreen();
                            p("장르가 삭제되었습니다.");
                            phase3.pause();
                        }
                    } catch (SQLException e) {
                        p("error: " + e.getMessage());
                    }
                    break;
                case 0:
                    return;
            }
        }
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void pause() {
        try {
            System.in.read();
        } catch (IOException e) {
        }
    }
}

class MovieData {
    int row;
    String title;
    String year;
    int movieId;
    int rating;

    MovieData(int row, String title, String year, int movieId) {
        this.row = row;
        this.title = title;
        this.year = year;
        this.movieId = movieId;
    }

    void setRating(int rating) {
        this.rating = rating;
    }
}
