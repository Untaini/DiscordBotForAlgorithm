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
										messages.add(new EmbedBuilder().appendDescription(String.format("<@!%d>���� [%d�� %s](%s) ������ �ذ��Ͽ����ϴ�!\n", user.getDiscordId(), problem.getID(), problem.getName(), api.getProblemLink(problem.getID()))).build());
								
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
			MessageEmbed message = getEmbedMessage("����� �̹� ��ϵ� �����Դϴ�.", RGBA(255,0,0,0));
			event.replyEmbeds(message).setEphemeral(true).queue();
    		return;
    	}
    	
		String baekjoonId = event.getOption("id").getAsString();
		SolvedacAPIManager api = new SolvedacAPIManager();
		if(!api.isBaekjoonId(baekjoonId)) {
			MessageEmbed message = getEmbedMessage("���ؿ� ��ϵ��� ���� ID�Դϴ�.", RGBA(255,0,0,0));
			event.replyEmbeds(message).setEphemeral(true).queue();
    		return;
		}
    	
    	userManager.addId(userId, baekjoonId);
    	MessageEmbed message = getEmbedMessage(String.format("���� ID ��� �Ϸ� (��ϵ� ID : %s)", baekjoonId), RGBA(34, 177, 16, 0));
		event.replyEmbeds(message).setEphemeral(true).queue();
		return;
    }
    
    public void editId(SlashCommandInteractionEvent event) {
    	long userId = event.getUser().getIdLong();
    	UserManager userManager = new UserManager();
    	
		String prevId = userManager.getUser(userId).getBaekjoonId(), nowId = event.getOption("id").getAsString();
    	if(prevId.equalsIgnoreCase(nowId)) {
    		MessageEmbed message = getEmbedMessage("��ϵ� ID�� �����մϴ�.", RGBA(255,0,0,0));
    		event.replyEmbeds(message).setEphemeral(true).queue();
    		return;
    	}
    	
		SolvedacAPIManager api = new SolvedacAPIManager();
		if(!api.isBaekjoonId(nowId)) {
			MessageEmbed message = getEmbedMessage("���ؿ� ��ϵ��� ���� ID�Դϴ�.", RGBA(255,0,0,0));
			event.replyEmbeds(message).setEphemeral(true).queue();
    		return;
		}
    	
		if(userManager.editId(userId, nowId)) {
			MessageEmbed message = getEmbedMessage(String.format("���� ID ���� �Ϸ� (���� �� ID : %s -> ���� �� ID : %s)", prevId, nowId), RGBA(34,177,16,0));
			event.replyEmbeds(message).setEphemeral(true).queue();
		}
		else {
    		MessageEmbed message = getEmbedMessage("����� ���� ��ϵ��� ���� �����Դϴ�.", RGBA(255,0,0,0));
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
    		MessageEmbed message = getEmbedMessage("����� ���� ��ϵ��� ���� �����Դϴ�.", RGBA(255,0,0,0));
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
    			//���� ������ ��Ȱ��ȭ �Ǿ��ִٴ� �ڵ�
        		MessageEmbed message = getEmbedMessage(String.format("%d���� ������ ���� ������ �ʾҽ��ϴ�.", week), RGBA(255,0,0,0));
        		event.replyEmbeds(message).setEphemeral(true).queue();
    			return;
    		}
    		
    		eb.setTitle(String.format("%d���� ����", week));
    		for(int cnt=0; cnt<homework.getCount(); ++cnt) {
    			Problem problem = homework.getProblem(cnt);
    			sb.append(String.format("%s\t [%d�� %s](%s)\n", user.getProblemStatus(week).get(cnt)?PASS_EMOJI:FAIL_EMOJI, problem.getID(), problem.getName(), api.getProblemLink(problem.getID())));
    		}
    	}
    	else {
    		//��ü ���� ���� ��൵
    		eb.setTitle("���� ���� ���ǥ");
    		for(int week=1; week<=HomeworkManager.totalWeek; ++week) {
    			if(!homeworkManager.getHomework(week).isActive())
    				continue;
    			
    			StringBuilder weekSb = new StringBuilder();
    			int passCnt = 0;
    			
    			weekSb.append("%d����   %d / %d   ");
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
    				//order ������ problemId�� �����ϴ� �ڵ�
    				if(order < 0 || order >= homework.getCount()) {
    					//�������� �ʴ� ������ �޽���
    		    		MessageEmbed message = getEmbedMessage("�ùٸ��� ���� �����Դϴ�.", RGBA(255,0,0,0));
    		    		event.replyEmbeds(message).setEphemeral(true).queue();
    					return;
    				}
					try {
						String problemName = api.getProblemName(problemId);
						if(problemName == null) {
				    		MessageEmbed message = getEmbedMessage(String.format("%d�� ������ ���ؿ� �������� �ʽ��ϴ�.", problemId), RGBA(255,0,0,0));
				    		event.replyEmbeds(message).setEphemeral(true).queue();
							return;
						}
						
						Problem preValues = homework.replaceProblem(order, problemId, problemName);
						userManager.allUsersClearProblem(week, order);
						//���� ���� �Ϸ� �ƴٴ� �޽���
			    		MessageEmbed message = getEmbedMessage(String.format("%d���� %d�� ���� ���� : %d�� %s -> %d�� %s", week, order+1, preValues.getID(), preValues.getName(), problemId, problemName), RGBA(34,177,16,0));
			    		event.replyEmbeds(message).setEphemeral(true).queue();
					}
					catch(Exception e) {
						//api ȣ�� ����, ��õ� �޽���
			    		MessageEmbed message = getEmbedMessage("API ȣ�� �� ������ �߻��߽��ϴ�. ��� �� �ٽ� �õ����ּ���.", RGBA(255,0,0,0));
			    		event.replyEmbeds(message).setEphemeral(true).queue();
					}
    			}
    			else {
    				//�� ������ ������ ������ �߰��ϴ� �ڵ�
					try {
						String problemName = api.getProblemName(problemId);
						homework.addProblem(problemId, problemName);
						userManager.allUsersUpdateHomework();
						//�߰� �Ϸ� �ߴٴ� �޽��� 

			    		MessageEmbed message = getEmbedMessage(String.format("%d���� %d�� ���� �߰� : %d�� %s", week, homework.getCount(), problemId, problemName), RGBA(34,177,16,0));
			    		event.replyEmbeds(message).setEphemeral(true).queue();
					} catch (Exception e) {
						//api ȣ�� ����, ��õ� �޽��� 
			    		MessageEmbed message = getEmbedMessage("API ȣ�� �� ������ �߻��߽��ϴ�. ��� �� �ٽ� �õ����ּ���.", RGBA(255,0,0,0));
			    		event.replyEmbeds(message).setEphemeral(true).queue();
					}
    			}
    		}
    		
    		else if(order != -1 && remove != null) {
    			//order������ ������ �����ϴ� �ڵ�

				if(order < 0 || order > homework.getCount()) {
					//�������� �ʴ� ������ �޽��� 
		    		MessageEmbed message = getEmbedMessage("�ùٸ��� ���� �����Դϴ�.", RGBA(255,0,0,0));
		    		event.replyEmbeds(message).setEphemeral(true).queue();
					return;
				}
    			//���� ���õ� ���� �����ߴٴ� �޽��� 
    			Problem removedValues = homework.removeProblem(order);
    			userManager.allUsersRemoveProblem(week, order);
	    		MessageEmbed message = getEmbedMessage(String.format("%d���� %d�� ���� ���� : %d�� %s", week, order+1, removedValues.getID(), removedValues.getName()), RGBA(34,177,16,0));
	    		event.replyEmbeds(message).setEphemeral(true).queue();
    		}
    		
    		else if(activation != null) {
    			//������ ������ Ȱ��ȭ/��Ȱ��ȭ �ϴ� �ڵ�
    			boolean active;
    			if(homework.isActive() == (active = activation.equalsIgnoreCase("active"))) {
    				//�̹� Ȱ��ȭ / ��Ȱ��ȭ �Ǿ� �ִٴ� �޽��� 
		    		MessageEmbed message = getEmbedMessage(String.format("�̹� %s�� �����Դϴ�.",active?"Ȱ��ȭ":"��Ȱ��ȭ"), RGBA(255,0,0,0));
		    		event.replyEmbeds(message).setEphemeral(true).queue();
		    		return;
    			}
    			
    			homework.setActive(active);
    			//Ȱ��ȭ / ��Ȱ��ȭ�� �ٲپ��ٴ� �޽���
	    		MessageEmbed message = getEmbedMessage(String.format("%d���� ���� : %s", week, active?"Ȱ��ȭ":"��Ȱ��ȭ"), RGBA(34,177,16,0));
	    		event.replyEmbeds(message).setEphemeral(true).queue();
    		}
    		
    		else if(problemId == -1 && order == -1 && activation == null && remove == null){
    			//������ ������ ��൵�� �����ִ� �޽���
    			EmbedBuilder eb = new EmbedBuilder();
    			eb.setTitle(String.format("%d���� (Ȱ��ȭ ���� : %s)",  week, homework.isActive()?PASS_EMOJI:FAIL_EMOJI));
    			
    			if(homework.getCount()>0) {
	    			StringBuilder sb = eb.getDescriptionBuilder();
	    			String format = new StringBuilder().append("%0").append((int)Math.log10(homework.getCount())+1).append('d').toString();
	    			for(int cnt=0; cnt<homework.getCount(); ++cnt) {
	    				Problem problem = homework.getProblem(cnt);
	    				sb.append(String.format("%s\t [%d�� %s](%s)\n", getNumberEmoji(String.format(format, cnt+1)), problem.getID(), problem.getName(), api.getProblemLink(problem.getID())));
	    			}
    			}
    			
	    		MessageEmbed message = eb.build();
	    		event.replyEmbeds(message).setEphemeral(true).queue();
    		}
    		
    		else {
    			//�߸��� ��ɾ� �Է��ߴٴ� �޽���
	    		MessageEmbed message = getEmbedMessage("�߸��� ��ɾ��Դϴ�.", RGBA(255,0,0,0));
	    		event.replyEmbeds(message).setEphemeral(true).queue();
    		}
    	}
    	else {
    		MessageEmbed message = getEmbedMessage("����� �����ڰ� �ƴմϴ�.", RGBA(255,0,0,0));
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
