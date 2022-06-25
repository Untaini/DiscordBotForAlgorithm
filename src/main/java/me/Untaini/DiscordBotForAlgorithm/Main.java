package me.Untaini.DiscordBotForAlgorithm;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
public class Main extends ListenerAdapter{

	public static void main(String[] args) throws LoginException {
		JDA jda = JDABuilder.createLight(JsonManager.getJsonFile("token.json").getAsJsonObject().get("token").getAsString(), Collections.emptyList())
				  .addEventListeners(new Main())
		          .build();
	
		jda.updateCommands()
			.addCommands(Commands.slash("register-id", "Register your baekjoon Id")
					.addOption(OptionType.STRING, "id", "Baekjoon Id", true))
			
			.addCommands(Commands.slash("edit-id", "Edit your baekjoon Id")
					.addOption(OptionType.STRING, "id", "Baekjoon Id", true))
			
			.addCommands(Commands.slash("homework", "Check homework status")
					.addOptions(new OptionData(OptionType.INTEGER, "week", String.format("Week Number(1~%d)",HomeworkManager.totalWeek)).setMinValue(1).setMaxValue(HomeworkManager.totalWeek)))
			
			.addCommands(Commands.slash("set-homework", "Set homework option per week")
					.addOptions(new OptionData(OptionType.INTEGER, "week", String.format("Week Number(1~%d)",HomeworkManager.totalWeek), true).setMinValue(1).setMaxValue(HomeworkManager.totalWeek))
					.addOption(OptionType.INTEGER, "order", "Order of the problem to be changed")
					.addOption(OptionType.INTEGER, "id", "Problem Id")
					.addOptions(new OptionData(OptionType.STRING, "activation","active/inactive").addChoice("active", "active").addChoice("inactive", "inactive"))
					.addOptions(new OptionData(OptionType.STRING, "remove","Remove selected Problem (must select order)").addChoice("remove", "remove")).setDefaultEnabled(false))
			
			.queue();
		
		
		Thread thread = new Thread(() -> {
			try {
				final int COOLDOWN = 60000;
				UserManager userManager = new UserManager();
				while(true) {
					try {
						Guild guild = jda.getGuildById(982916403797573642L);
						if(guild != null) {
							UserManager.saveData();
							HomeworkManager.saveData();
							Map<User, List<Problem>> solvedMap = userManager.allUsersCheckHomework();
							
							if(!solvedMap.isEmpty()) {
								List<MessageEmbed> messages = new ArrayList<>();
								SolvedacAPIManager api = new SolvedacAPIManager();
								
								for(User user : solvedMap.keySet()) 
									for(Problem problem : solvedMap.get(user))
										messages.add(new EmbedBuilder().appendDescription(String.format("<@!%d>님이 [%d번 %s](%s) 문제를 해결하였습니다!\n", user.getDiscordId(), problem.getID(), problem.getName(), api.getProblemLink(problem.getID()))).build());
								
								if(!messages.isEmpty())
									guild.getTextChannelById(987996968934580274L).sendMessageEmbeds(messages).queue();
							}
						}
					}
					catch(Exception e) {
						Thread.sleep(COOLDOWN*10);
					}
					Thread.sleep(COOLDOWN);	
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		thread.setDaemon(true);
		thread.run();
	}
	

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {
    	switch(event.getName()) {
    	case "register-id" -> registerId(event);
    	case "edit-id" -> editId(event);
    	case "homework" -> homework(event);
    	case "set-homework" -> setHomework(event); //only admin
    	default -> event.reply("unknown command").setEphemeral(true).queue();
    	}
    }
    
    public void registerId(SlashCommandInteractionEvent event) {
    	long userId = event.getUser().getIdLong();
    	UserManager userManager = new UserManager();
    	
    	if(userManager.checkRegister(userId)) {
			MessageEmbed message = getEmbedMessage("당신은 이미 등록된 유저입니다.", RGBA(255,0,0,0));
			event.replyEmbeds(message).setEphemeral(true).queue();
    		return;
    	}
    	
		String baekjoonId = event.getOption("id").getAsString();
		SolvedacAPIManager api = new SolvedacAPIManager();
		if(!api.isBaekjoonId(baekjoonId)) {
			MessageEmbed message = getEmbedMessage("백준에 등록되지 않은 ID입니다.", RGBA(255,0,0,0));
			event.replyEmbeds(message).setEphemeral(true).queue();
    		return;
		}
    	
    	userManager.addId(userId, baekjoonId);
    	MessageEmbed message = getEmbedMessage(String.format("백준 ID 등록 완료 (등록된 ID : %s)", baekjoonId), RGBA(34, 177, 16, 0));
		event.replyEmbeds(message).setEphemeral(true).queue();
		return;
    }
    
    public void editId(SlashCommandInteractionEvent event) {
    	long userId = event.getUser().getIdLong();
    	UserManager userManager = new UserManager();
    	
		String prevId = userManager.getUser(userId).getBaekjoonId(), nowId = event.getOption("id").getAsString();
    	if(prevId.equalsIgnoreCase(nowId)) {
    		MessageEmbed message = getEmbedMessage("등록된 ID와 동일합니다.", RGBA(255,0,0,0));
    		event.replyEmbeds(message).setEphemeral(true).queue();
    		return;
    	}
    	
		SolvedacAPIManager api = new SolvedacAPIManager();
		if(!api.isBaekjoonId(nowId)) {
			MessageEmbed message = getEmbedMessage("백준에 등록되지 않은 ID입니다.", RGBA(255,0,0,0));
			event.replyEmbeds(message).setEphemeral(true).queue();
    		return;
		}
    	
		if(userManager.editId(userId, nowId)) {
			MessageEmbed message = getEmbedMessage(String.format("백준 ID 변경 완료 (변경 전 ID : %s -> 변경 후 ID : %s)", prevId, nowId), RGBA(34,177,16,0));
			event.replyEmbeds(message).setEphemeral(true).queue();
		}
		else {
    		MessageEmbed message = getEmbedMessage("당신은 아직 등록되지 않은 유저입니다.", RGBA(255,0,0,0));
    		event.replyEmbeds(message).setEphemeral(true).queue();
		}
    	return;
    }
    
    final String PASS_EMOJI = ":white_check_mark:", FAIL_EMOJI = "<:white_deny_mark:986640726832054394>";
    
    public void homework(SlashCommandInteractionEvent event) {
		UserManager userManager = new UserManager();
		HomeworkManager homeworkManager = new HomeworkManager();
		User user = userManager.getUser(event.getUser().getIdLong());
		
		if(user == null) {
    		MessageEmbed message = getEmbedMessage("당신은 아직 등록되지 않은 유저입니다.", RGBA(255,0,0,0));
    		event.replyEmbeds(message).setEphemeral(true).queue();
			return;
		}
		
		SolvedacAPIManager api = new SolvedacAPIManager();
		EmbedBuilder eb = new EmbedBuilder();
		StringBuilder sb = eb.getDescriptionBuilder();
    	
    	if(event.getOption("week") != null) {
    		int week = event.getOption("week").getAsInt();
    		WeekHomework homework = homeworkManager.getHomework(week);
    		
    		if(!homework.isActive()) {
    			//대충 과제가 비활성화 되어있다는 코드
        		MessageEmbed message = getEmbedMessage(String.format("%d주차 과제는 아직 열리지 않았습니다.", week), RGBA(255,0,0,0));
        		event.replyEmbeds(message).setEphemeral(true).queue();
    			return;
    		}
    		
    		eb.setTitle(String.format("%d주차 과제", week));
    		for(int cnt=0; cnt<homework.getCount(); ++cnt) {
    			Problem problem = homework.getProblem(cnt);
    			sb.append(String.format("%s\t [%d번 %s](%s)\n", user.getProblemStatus(week).get(cnt)?PASS_EMOJI:FAIL_EMOJI, problem.getID(), problem.getName(), api.getProblemLink(problem.getID())));
    		}
    	}
    	else {
    		//전체 과제 수행 요약도
    		eb.setTitle("과제 수행 요약표");
    		for(int week=1; week<=HomeworkManager.totalWeek; ++week) {
    			if(!homeworkManager.getHomework(week).isActive())
    				continue;
    			
    			StringBuilder weekSb = new StringBuilder();
    			int passCnt = 0;
    			
    			weekSb.append("%d주차   %d / %d   ");
    			for(boolean isPassed : user.getProblemStatus(week)) {
    				weekSb.append(isPassed?PASS_EMOJI:FAIL_EMOJI).append(" ");
    				passCnt += isPassed?1:0;
    			}
    			weekSb.append("\n");
    			
    			sb.append(String.format(weekSb.toString(), week, passCnt, user.getProblemStatus(week).size()));
    		}
    	}
    	
		event.replyEmbeds(eb.build()).setEphemeral(true).queue();
		return;
    }
    
    public void setHomework(SlashCommandInteractionEvent event) {
    	if(event.getMember().hasPermission(Permission.ADMINISTRATOR)){
    		int week = event.getOption("week").getAsInt(), problemId = -1, order = -1;
    		String activation = null, remove = null;
    		WeekHomework homework = new HomeworkManager().getHomework(week);
    		SolvedacAPIManager api = new SolvedacAPIManager();
    		UserManager userManager = new UserManager();
    		
    		if(event.getOption("id") != null) 
    			problemId = event.getOption("id").getAsInt();
			if(event.getOption("order") != null) 
				order = event.getOption("order").getAsInt()-1;
    		if(event.getOption("activation") != null)
    			activation = event.getOption("activation").getAsString();
    		if(event.getOption("remove") != null)
    			remove = event.getOption("remove").getAsString();
    		
    		if(problemId != -1) {
    			if(order != -1) {
    				//order 문제를 problemId로 변경하는 코드
    				if(order < 0 || order >= homework.getCount()) {
    					//존재하지 않는 선택지 메시지
    		    		MessageEmbed message = getEmbedMessage("올바르지 않은 선택입니다.", RGBA(255,0,0,0));
    		    		event.replyEmbeds(message).setEphemeral(true).queue();
    					return;
    				}
					try {
						String problemName = api.getProblemName(problemId);
						if(problemName == null) {
				    		MessageEmbed message = getEmbedMessage(String.format("%d번 문제는 백준에 존재하지 않습니다.", problemId), RGBA(255,0,0,0));
				    		event.replyEmbeds(message).setEphemeral(true).queue();
							return;
						}
						
						Problem preValues = homework.replaceProblem(order, problemId, problemName);
						userManager.allUsersClearProblem(week, order);
						//대충 변경 완료 됐다는 메시지
			    		MessageEmbed message = getEmbedMessage(String.format("%d주차 %d번 문제 변경 : %d번 %s -> %d번 %s", week, order+1, preValues.getID(), preValues.getName(), problemId, problemName), RGBA(34,177,16,0));
			    		event.replyEmbeds(message).setEphemeral(true).queue();
					}
					catch(Exception e) {
						//api 호출 실패, 재시도 메시지
			    		MessageEmbed message = getEmbedMessage("API 호출 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", RGBA(255,0,0,0));
			    		event.replyEmbeds(message).setEphemeral(true).queue();
					}
    			}
    			else {
    				//새 문제를 선택한 주차에 추가하는 코드
					try {
						String problemName = api.getProblemName(problemId);
						homework.addProblem(problemId, problemName);
						userManager.allUsersUpdateHomework();
						//추가 완료 했다는 메시지 

			    		MessageEmbed message = getEmbedMessage(String.format("%d주차 %d번 문제 추가 : %d번 %s", week, homework.getCount(), problemId, problemName), RGBA(34,177,16,0));
			    		event.replyEmbeds(message).setEphemeral(true).queue();
					} catch (Exception e) {
						//api 호출 실패, 재시도 메시지 
			    		MessageEmbed message = getEmbedMessage("API 호출 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", RGBA(255,0,0,0));
			    		event.replyEmbeds(message).setEphemeral(true).queue();
					}
    			}
    		}
    		
    		else if(order != -1 && remove != null) {
    			//order순번의 문제를 삭제하는 코드

				if(order < 0 || order > homework.getCount()) {
					//존재하지 않는 선택지 메시지 
		    		MessageEmbed message = getEmbedMessage("올바르지 않은 선택입니다.", RGBA(255,0,0,0));
		    		event.replyEmbeds(message).setEphemeral(true).queue();
					return;
				}
    			//대충 선택된 문제 삭제했다는 메시지 
    			Problem removedValues = homework.removeProblem(order);
    			userManager.allUsersRemoveProblem(week, order);
	    		MessageEmbed message = getEmbedMessage(String.format("%d주차 %d번 문제 삭제 : %d번 %s", week, order+1, removedValues.getID(), removedValues.getName()), RGBA(34,177,16,0));
	    		event.replyEmbeds(message).setEphemeral(true).queue();
    		}
    		
    		else if(activation != null) {
    			//선택한 주차를 활성화/비활성화 하는 코드
    			boolean active;
    			if(homework.isActive() == (active = activation.equalsIgnoreCase("active"))) {
    				//이미 활성화 / 비활성화 되어 있다는 메시지 
		    		MessageEmbed message = getEmbedMessage(String.format("이미 %s인 상태입니다.",active?"활성화":"비활성화"), RGBA(255,0,0,0));
		    		event.replyEmbeds(message).setEphemeral(true).queue();
		    		return;
    			}
    			
    			homework.setActive(active);
    			//활성화 / 비활성화로 바꾸었다는 메시지
	    		MessageEmbed message = getEmbedMessage(String.format("%d주차 과제 : %s", week, active?"활성화":"비활성화"), RGBA(34,177,16,0));
	    		event.replyEmbeds(message).setEphemeral(true).queue();
    		}
    		
    		else if(problemId == -1 && order == -1 && activation == null && remove == null){
    			//선택한 주차의 요약도를 보여주는 메시지
    			EmbedBuilder eb = new EmbedBuilder();
    			eb.setTitle(String.format("%d주차 (활성화 여부 : %s)",  week, homework.isActive()?PASS_EMOJI:FAIL_EMOJI));
    			
    			if(homework.getCount()>0) {
	    			StringBuilder sb = eb.getDescriptionBuilder();
	    			String format = new StringBuilder().append("%0").append((int)Math.log10(homework.getCount())+1).append('d').toString();
	    			for(int cnt=0; cnt<homework.getCount(); ++cnt) {
	    				Problem problem = homework.getProblem(cnt);
	    				sb.append(String.format("%s\t [%d번 %s](%s)\n", getNumberEmoji(String.format(format, cnt+1)), problem.getID(), problem.getName(), api.getProblemLink(problem.getID())));
	    			}
    			}
    			
	    		MessageEmbed message = eb.build();
	    		event.replyEmbeds(message).setEphemeral(true).queue();
    		}
    		
    		else {
    			//잘못된 명령어 입력했다는 메시지
	    		MessageEmbed message = getEmbedMessage("잘못된 명령어입니다.", RGBA(255,0,0,0));
	    		event.replyEmbeds(message).setEphemeral(true).queue();
    		}
    	}
    	else {
    		MessageEmbed message = getEmbedMessage("당신은 관리자가 아닙니다.", RGBA(255,0,0,0));
    		event.replyEmbeds(message).setEphemeral(true).queue();
    	}
    	
    	return;
    }
    
    private int RGBA(int red, int blue, int green, int alpha) {
    	Color rgb = new Color(red, blue, green, alpha);
    	return rgb.getRGB();
    }
    
    private MessageEmbed getEmbedMessage(String description, int color) {
    	MessageEmbed message = new MessageEmbed(null, null, description, EmbedType.RICH, null, color, null, null, null, null, null, null, null);
    	return message;
    }
      
    final String[] numberEmoji = {":zero:", ":one:", ":two:", ":three:", ":four:", ":five:", ":six:", ":seven:", ":eight:", ":nine:"}; 
    private String getNumberEmoji(String number) {    	
    	for(int cnt=0; cnt<10; ++cnt)
    		number = number.replaceAll(String.valueOf(cnt), numberEmoji[cnt]);
    	
    	return number;
    }
}
