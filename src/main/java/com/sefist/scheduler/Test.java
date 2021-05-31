package com.sefist.scheduler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Test {

//    @Autowired
//    EmsService emsService;

	@RequestMapping("/send/{id}")
	public String putTest(@PathVariable int id) {
		Date nowDate = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
		String strNowDate = simpleDateFormat.format(nowDate);
		String rptNo = strNowDate.substring(0, 4) + "-" + strNowDate.substring(8, strNowDate.length());
		String docKind = "";

		String fileName = "";
		switch (id) {
		// temp파일에서 생성 => 파일요구간격 이상 테스트
		case 1:
			fileName = makeFileFromTemp().getName();
			docKind = "REP001";
			break;
		// 1회전송 크기 생성
		case 2:
			fileName = makeFile(2, null).getName();
			docKind = "REP001";
			break;
		// FIU003 문서생성
		case 3:
			fileName = makeFile(3, rptNo).getName();
			docKind = "FIU003";
			break;
		default:
			fileName = makeFile(2, null).getName();
			docKind = "REP001";
			break;
		}

		String sendMessage = "";
		// 인터페이스ID(10)
		sendMessage += String.format("%10s", "CAP1PAS001");
		// 기관코드(6)
		sendMessage += "FC0615";
		// 보고문서번호(20)
		sendMessage += String.format("%20s", rptNo);
		// 문서종류코드(10)
		sendMessage += String.format("%10s", docKind);
		// 보고문서메시지유형코드(2)
		sendMessage += "01";
		// 종전보고문서번호(20)
		sendMessage += String.format("%20s", rptNo);
		// 기본보고문서번호(20)
		sendMessage += String.format("%20s", rptNo);
		// 거래순번(15)
		sendMessage += "0000001/0000001";
		// 보고파일명(100)
		sendMessage += String.format("%100s", fileName);
		// 중계기관코드(6)
		sendMessage += String.format("%6s", "DH0002");
		// 보고사용자ID(30)
		sendMessage += String.format("%30s", "ingyu97");
		// 등록일자(8)
		sendMessage += strNowDate.substring(0, 8);
		// 재송신횟수(2)
		sendMessage += "00";
		sendMessage = String.format("%04d", sendMessage.length() + 4) + sendMessage;

		return fileName + " : Send OK!!";
	}

	@RequestMapping("/conn/{id}")
	public String getTest(@PathVariable int id) {
		return "Connected OK !!";
	}

	public File makeFileFromTemp() {
		Date nowDate = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("_yyyyMMdd_HHmmssS");
		String strNowDate = simpleDateFormat.format(nowDate);
		String name = "CTR_FC0615" + strNowDate + ".SND";
//        String sourceName = emsService.getProp().getSendFilePath() + File.separator + "temp.txt";
//        String targetName = emsService.getProp().getSendFilePath() + File.separator +name ;
		String sourceName = "";
		String targetName = "";

		Path source = Paths.get(sourceName);
		Path target = Paths.get(targetName);

		// StandardCopyOption.REPLACE_EXISTING : 파일이 이미 존재할 경우 덮어쓰기
		try {
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return new File(targetName);
	}

	public File makeFile(int id, String rptNo) {
		Date nowDate = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("_yyyyMMdd_HHmmssS");
		SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddHHmmss");
		String strNowDate = simpleDateFormat.format(nowDate);
		String name = "CTR_FC0615" + strNowDate + ".SND";

		File file = null;
		FileWriter fw = null;

		String txt = "";
		if (id == 2) {
			txt = String.valueOf(UUID.randomUUID());
		} else if (id == 3) {
			txt = "CTRSTART||03||01||FC0615||" + rptNo + "||" + simpleDateFormat2.format(nowDate)
					+ "||00||접수성공||CTREND";
		}

//        String fileName = emsService.getProp().getSendFilePath() + File.separator + name ;
		String fileName = "";
		try {
			file = new File(fileName);
			fw = new FileWriter(file, true);
			fw.write(txt);
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fw != null)
				try {
					fw.close();
				} catch (IOException e) {

				}
		}
		return file;
	}

}