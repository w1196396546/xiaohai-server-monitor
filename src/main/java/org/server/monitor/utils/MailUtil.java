package org.server.monitor.utils;

import cn.hutool.extra.mail.MailAccount;

import java.util.List;

public class MailUtil {
	private MailUtil() {}
	private static MailAccount account;

	public static MailAccount getAccount() {
		if (account == null) {
			synchronized (MailUtil.class) {
				if (account == null) {
					account = new MailAccount();
					account.setHost(ConfigUtil.SETTING.getStr("mail.host"));
					account.setPort(ConfigUtil.SETTING.getInt("mail.port"));
					account.setSslEnable(ConfigUtil.SETTING.getBool("mail.sslEnable"));
					account.setFrom(ConfigUtil.SETTING.getStr("mail.from"));

					Boolean auth = ConfigUtil.SETTING.getBool("mail.auth");
					account.setAuth(auth);
					if (auth) {
						account.setUser(ConfigUtil.SETTING.getStr("mail.user"));
						account.setPass(ConfigUtil.SETTING.getStr("mail.pass"));
					}
				}
			}
		}
		return account;
	}


	public static void sendEMail(String title, String body, List<String> mails , boolean isHtml) {
		MailAccount account = getAccount();
		cn.hutool.extra.mail.MailUtil.send(account, mails, title, body, isHtml);
	}
}
