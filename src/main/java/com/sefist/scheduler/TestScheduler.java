package com.sefist.scheduler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	private static int procCnt = 0;
	private Environment env;
	private String[] rptMsgTypeCds = { "01", "02", "03", "04", "05", "96", "97", "98", "99" };
	private String[] ips = { "api.ip1", "api.ip2" };
	private String[] fileSize = { "3072", "11264", "179200", "12582912" };

	public TestScheduler(Environment env) {
		this.env = env;
	}

	@Scheduled(cron = "*/3 * * * * *")
	public void randomAddMt() {
		int endCnt = Integer.parseInt(env.getProperty("api.proc.endcnt"));

		if (procCnt < endCnt) {
			File f = null;
			Random ran = new Random();
			String fileMod = env.getProperty("api.file.mod");
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String path = env.getProperty("api.file.dir");
			int ipIdx = ran.nextInt(2);
			String ip = env.getProperty(ips[ipIdx]);
			String url = String.format("http://%s/api/fusendmt", ip);
			String secretid = env.getProperty("api.hash");
//			String test = String.valueOf(ran.nextBoolean());
			String test = "false";
			String rptFcltyCd = "FC0615";
			String docKndCd = "REP001";
			String rptDocNo = sdf.format(new Date());
			String rptStatusCd = "SR";
			String rptProcType = "00";

			if (fileMod.equals("0"))
				f = makeFile(rptDocNo, path, fileSize[0]);
			else if (fileMod.equals("1"))
				f = makeFile(rptDocNo, path, fileSize[1]);
			else if (fileMod.equals("2"))
				f = makeFile(rptDocNo, path, fileSize[2]);
			else if (fileMod.equals("3"))
				f = makeFile(rptDocNo, path, fileSize[3]);
			else
				f = makeFile(rptDocNo, path, fileSize[ran.nextInt(4)]);
			log.info(f.toString());

			String rptMsgTypeCd = rptMsgTypeCds[ran.nextInt(9)];
			String preRptDocNo = rptDocNo;
			String basRptDocNo = rptDocNo;
			String trnstnOrder = env.getProperty("api.trnstnorder");
			String rptFileNm = f.getName();
			String centAdminCd = "DH0002";
			String rptUserId = "testid";

			if (f != null) {
				sb.setLength(0);
				sb.append(String.format(
						"%s?secretid=%s&test=%s&rptFcltyCd=%s&rptDocNo=%s&rptStatusCd=%s&rptProcType=%s&docKndCd=%s&rptMsgTypeCd=%s&preRptDocNo=%s&basRptDocNo=%s&trnstnOrder=%s&rptFileNm=%s&centAdminCd=%s&rptUserId=%s",
						url, secretid, test, rptFcltyCd, rptDocNo, rptStatusCd, rptProcType, docKndCd, rptMsgTypeCd,
						preRptDocNo, basRptDocNo, trnstnOrder, rptFileNm, centAdminCd, rptUserId));

				log.info(sb.toString());

				httpRequest(sb.toString());

				log.info(String.format("cnt : %d", procCnt));
				
				procCnt += 1;
			}
		}
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

	public File makeFile(String rptDocNo, String path, String size) {
		String txt = String.format("%-" + size + "s", "A").replace(" ", "A");
		String fileName = String.format("%s/CTR_FC0615%s.SND", path, rptDocNo);

		File file = null;
		FileWriter fw = null;

		try {
			file = new File(fileName);
			fw = new FileWriter(file, true);
			fw.write(txt);
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fw != null)
					fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return file;
	}

	@SuppressWarnings("unused")
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