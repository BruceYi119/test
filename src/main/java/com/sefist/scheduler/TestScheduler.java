package com.sefist.scheduler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
public class TestScheduler implements SchedulingConfigurer {

	private static final Logger log = LoggerFactory.getLogger(TestScheduler.class);
	private Environment env;
	private String[] doc_knd_cd = { "REP001", "REP002", "REP003" };
	private String[] rpt_msg_type_cd = { "01", "02", "03", "04", "05", "96", "97", "98", "99" };

	public TestScheduler(Environment env) {
		this.env = env;
	}

	@Scheduled(cron = "*/6 * * * * *")
	public void randomAddMt() {
		Random ran = new Random();
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String url = String.format("http://%s/api/fusendmt", env.getProperty("api.ip"));
		String secretid = env.getProperty("api.hash");
		String test = String.valueOf(ran.nextBoolean());
		String rptFcltyCd = getRandomAlphabet();
		String docKndCd = doc_knd_cd[ran.nextInt(3)];
		String rptDocNo = sdf.format(new Date());
		String rptStatusCd = "SR";
		String rptProcType = "00";
		String rptMsgTypeCd = rpt_msg_type_cd[ran.nextInt(9)];
		String preRptDocNo = rptDocNo;
		String basRptDocNo = rptDocNo;
		String trnstnOrder = env.getProperty("api.trnstnorder");
		String rptFileNm = String.format("CTR_FC0615_%d.%s", ran.nextInt(10), env.getProperty("api.ext"));
		String centAdminCd = "DH0002";
		String rptUserId = "testid";

		sb.setLength(0);
		sb.append(String.format(
				"%s?secretid=%s&test=%s&rptFcltyCd=%s&rptDocNo=%s&rptStatusCd=%s&rptProcType=%s&docKndCd=%s&rptMsgTypeCd=%s&preRptDocNo=%s&basRptDocNo=%s&trnstnOrder=%s&rptFileNm=%s&centAdminCd=%s&rptUserId=%s",
				url, secretid, test, rptFcltyCd, rptDocNo, rptStatusCd, rptProcType, docKndCd, rptMsgTypeCd,
				preRptDocNo, basRptDocNo, trnstnOrder, rptFileNm, centAdminCd, rptUserId));

		log.info(sb.toString());

		httpRequest(sb.toString());
	}

	public void httpRequest(String targetUrl) {
		HttpURLConnection con = null;

		try {
			URL url = new URL(targetUrl);
			con = (HttpURLConnection) url.openConnection();

			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String inputLine;

			while ((inputLine = br.readLine()) != null)
				sb.append(inputLine);

			log.info(sb.toString());
		} catch (Exception e) {
			log.info(String.format("httpRequest error %s", e.getMessage()));
		} finally {
			if (con != null)
				con.disconnect();
		}
	}

	private String getRandomAlphabet() {
		StringBuilder sb = new StringBuilder();
		Random ran = new Random();

		for (int i = 0; i < 6; i++)
			sb.append(Character.toString((char) ((int) ran.nextInt(26) + 65)));

		return sb.toString();
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		ThreadPoolTaskScheduler tpts = new ThreadPoolTaskScheduler();
		tpts.setPoolSize(2);
		tpts.setThreadNamePrefix("testSche-");
		tpts.initialize();
		taskRegistrar.setTaskScheduler(tpts);
	}

}