package me.Untaini.DiscordBotForAlgorithm;
import java.awt.Color;
import java.util.Collections;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public class Main extends ListenerAdapter{

	public static void main(String[] args) throws LoginException {
		JDA jda = JDABuilder.createLight((String)JSONManager.getJSONFile("token.json").get("token"), Collections.emptyList())
				  .addEventListeners(new Main())
		          .build();
	
		jda.updateCommands()
			.addCommands(Commands.slash("register-id", "Register your baekjoon Id").addOption(OptionType.STRING, "id", "Baekjoon Id", true))
			.addCommands(Commands.slash("edit-id", "Edit your baekjoon Id").addOption(OptionType.STRING, "id", "Baekjoon Id", true))
			.addCommands(Commands.slash("homework", "Check homework status").addOptions(new OptionData(OptionType.INTEGER, "week", "Week Number(1~8)").setMinValue(1).setMaxValue(8)))
			.addCommands(Commands.slash("set-homework", "Set homework option per week").addOptions(new OptionData(OptionType.INTEGER, "week", "Week Number(1~8)", true).setMinValue(1).setMaxValue(8))
					.addOption(OptionType.INTEGER, "order", "Order of the problem to be changed").addOption(OptionType.INTEGER, "id", "Problem Id")
					.addOptions(new OptionData(OptionType.STRING, "activation","active/inactive").addChoice("active", "active").addChoice("inactive", "inactive"))
					.addOptions(new OptionData(OptionType.STRING, "remove","Remove selected Problem (must select order)").addChoice("remove", "remove")).setDefaultEnabled(false))
			.queue();
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
    	if(!userManager.checkRegister(userId)) {
	    	userManager.addId(event.getUser().getIdLong(), event.getOption("id").getAsString());
    		MessageEmbed message = getEmbedMessage(new StringBuilder().append("성공적으로 ").append(event.getOption("id").getAsString()).append("를 등록하였습니다.").toString(), RGBA(34,177,16,0));
    		event.replyEmbeds(message).setEphemeral(true).queue();
    	}
    	else {
			MessageEmbed message = getEmbedMessage("당신은 이미 등록된 유저입니다.", RGBA(255,0,0,0));
			event.replyEmbeds(message).setEphemeral(true).queue();
    	}
    }
    
    public void editId(SlashCommandInteractionEvent event) {
    	long userId = event.getUser().getIdLong();
    	UserManager userManager = new UserManager();
    	if(userManager.checkRegister(userId)) {
    		String prevId = userManager.getUser(userId).getBaekjoonId(), nowId = event.getOption("id").getAsString();
    		if(!prevId.equalsIgnoreCase(nowId)) {
	    		userManager.editId(userId, nowId);
	    		MessageEmbed message = getEmbedMessage(new StringBuilder().append("성공적으로 ").append(prevId).append("에서 ").append(nowId).append("로 변경하였습니다.").toString(), RGBA(34,177,16,0));
	    		event.replyEmbeds(message).setEphemeral(true).queue();
    			
    		}
    		else {
        		MessageEmbed message = getEmbedMessage("동일한 ID입니다.", RGBA(255,0,0,0));
        		event.replyEmbeds(message).setEphemeral(true).queue();
    		}
    	}
    	else {
    		MessageEmbed message = getEmbedMessage("당신은 아직 등록되지 않은 유저입니다.", RGBA(255,0,0,0));
    		event.replyEmbeds(message).setEphemeral(true).queue();
    	}
    	
    	
    }
    
    final String passEmoji = ":white_check_mark: ", failEmoji = ":white_deny_mark: ";
    
    public void homework(SlashCommandInteractionEvent event) {
    	
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
    					//존재하지 않는 선택지 메시지 출력
    					return;
    				}
					try {
						String problemName = api.getProblemName(problemId);
						Pair<Integer, String> preValues = homework.replaceProblem(order, problemId, problemName);
						userManager.allUsersClearProblem(week, order);
						//대충 변경 완료 됐다는 메시지 출력
					}
					catch(Exception e) {
						//api 호출 실패, 재시도 메시지 출력 + 존재하지 않는 문제id 인 경우
					}
    			}
    			else {
    				//새 문제를 선택한 주차에 추가하는 코드
					try {
						String problemName = api.getProblemName(problemId);
						homework.addProblem(problemId, problemName);
						//추가 완료 했다는 메시지 
					} catch (Exception e) {
						//api 호출 실패, 재시도 메시지 
					}
    			}
    		}
    		
    		else if(order != -1 && remove != null) {
    			//order순번의 문제를 삭제하는 코드

				if(order < 0 || order > homework.getCount()) {
					//존재하지 않는 선택지 메시지 
					return;
				}
    			Pair<Integer, String> removedValues = homework.removeProblem(order);
    			userManager.allUsersRemoveProblem(week, order);
    			
    			//대충 선택된 문제 삭제했다는 메시지 
    		}
    		
    		else if(activation != null) {
    			//선택한 주차를 활성화/비활성화 하는 코드
    			boolean active;
    			if(homework.isActive() == (active = activation.equalsIgnoreCase("active"))) {
    				//이미 활성화 / 비활성화 되어 있다는 메시지 
    			}
    			
    			homework.setActive(active);
    			//활성화 / 비활성화로 바꾸었다는 메시지
    		}
    		
    		else if(problemId == -1 && order == -1 && activation == null && remove == null){
    			//선택한 주차의 요약도를 보여주는 메시지
    		}
    		
    		else {
    			//잘못된 명령어 입력했다는 메시지
    		}
    		
    		MessageEmbed message = getEmbedMessage("", RGBA(34,177,16,0));
    		event.replyEmbeds(message).setEphemeral(true).queue();
    	}
    	else {
    		MessageEmbed message = getEmbedMessage("당신은 관리자가 아닙니다.", RGBA(255,0,0,0));
    		event.replyEmbeds(message).setEphemeral(true).queue();
    	}
    }
    
    private int RGBA(int red, int blue, int green, int alpha) {
    	Color rgb = new Color(red, blue, green, alpha);
    	return rgb.getRGB();
    }
    
    private MessageEmbed getEmbedMessage(String description, int color) {
    	MessageEmbed message = new MessageEmbed(null, null, description, EmbedType.RICH, null, color, null, null, null, null, null, null, null);
    	return message;
    	
    }

}
