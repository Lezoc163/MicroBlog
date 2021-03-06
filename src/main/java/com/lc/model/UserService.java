package com.lc.model;

import java.io.*;
import java.util.*;

/**
 * @author DELL
 * @date 2021/12/19 10:56
 */
public class UserService {
    private LinkedList<Blah> newest = new LinkedList<>();
    private AccountDAO accountDAO;
    private BlahDAO blahDAO;
    private MailCarrier mailCarrier;
    private String template;

    public UserService(String USERS,
                       AccountDAO userDAO, BlahDAO blahDAO) {
        this(userDAO, blahDAO);
    }

    public UserService(AccountDAO userDAO, BlahDAO blahDAO) {
        this.accountDAO = userDAO;
        this.blahDAO = blahDAO;
    }

    public UserService(AccountDAO userDAO, BlahDAO blahDAO, MailCarrier mailCarrier) {
        this(userDAO, blahDAO);
        this.mailCarrier = mailCarrier;
    }

    public boolean isUserExisted(Account account) {
        return accountDAO.isUserExisted(account);
    }

    public void add(Account account) {
        accountDAO.addAccount(account);
    }

    public boolean checkLogin(Account account) {
        if (account.getName() != null &&
                account.getPassword() != null) {
            Account storeAcct = accountDAO.getAccount(account);
            return storeAcct != null &&
                    storeAcct.getPassword().equals(account.getPassword());
        }
        return false;
    }

    /**
     * TreeMap排序用，因为希望信息的日期越近的在越上头显示
     */
    private class DateComparator implements Comparator<Blah> {
        @Override
        public int compare(Blah b1, Blah b2) {
            return -b1.getDate().compareTo(b2.getDate());
        }
    }

    private DateComparator comparator = new DateComparator();

    public List<Blah> getBlahs(Blah blah) {
        List<Blah> blahs = blahDAO.getBlahs(blah);
        Collections.sort(blahs, comparator);
        return blahs;
    }

    public void addBlah(Blah blah) throws IOException {
        blahDAO.addBlah(blah);
        newest.addFirst(blah);
        if (newest.size() > 20) {
            newest.removeLast();
        }
    }

    public void deleteBlah(Blah blah) {
        blahDAO.deleteBlah(blah);
        newest.remove(blah);
    }

    public List<Blah> getNewest() {
        return newest;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public boolean sendPasswordTo(Account account) {
        Account acct = accountDAO.getAccount(account);
        if (acct != null && acct.getEmail().equals(account.getEmail())) {
            String subject = account.getName() + "的微博密码";
            String content = null;
            if (template == null) {
                content = account.getName() + "您好！您的密码是：" +
                        acct.getPassword();
            } else {
                content = template.replace("#name", account.getName())
                        .replace("#password", acct.getPassword());
            }
            mailCarrier.sendTo(account, subject, content);
            return true;
        }
        return false;
    }
}
