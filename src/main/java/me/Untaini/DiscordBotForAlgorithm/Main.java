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
    		MessageEmbed message = getEmbedMessage(new StringBuilder().append("���������� ").append(event.getOption("id").getAsString()).append("�� ����Ͽ����ϴ�.").toString(), RGBA(34,177,16,0));
    		event.replyEmbeds(message).setEphemeral(true).queue();
    	}
    	else {
			MessageEmbed message = getEmbedMessage("����� �̹� ��ϵ� �����Դϴ�.", RGBA(255,0,0,0));
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
	    		MessageEmbed message = getEmbedMessage(new StringBuilder().append("���������� ").append(prevId).append("���� ").append(nowId).append("�� �����Ͽ����ϴ�.").toString(), RGBA(34,177,16,0));
	    		event.replyEmbeds(message).setEphemeral(true).queue();
    			
    		}
    		else {
        		MessageEmbed message = getEmbedMessage("������ ID�Դϴ�.", RGBA(255,0,0,0));
        		event.replyEmbeds(message).setEphemeral(true).queue();
    		}
    	}
    	else {
    		MessageEmbed message = getEmbedMessage("����� ���� ��ϵ��� ���� �����Դϴ�.", RGBA(255,0,0,0));
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
    				//order ������ problemId�� �����ϴ� �ڵ�
    				if(order < 0 || order >= homework.getCount()) {
    					//�������� �ʴ� ������ �޽��� ���
    					return;
    				}
					try {
						String problemName = api.getProblemName(problemId);
						Pair<Integer, String> preValues = homework.replaceProblem(order, problemId, problemName);
						userManager.allUsersClearProblem(week, order);
						//���� ���� �Ϸ� �ƴٴ� �޽��� ���
					}
					catch(Exception e) {
						//api ȣ�� ����, ��õ� �޽��� ��� + �������� �ʴ� ����id �� ���
					}
    			}
    			else {
    				//�� ������ ������ ������ �߰��ϴ� �ڵ�
					try {
						String problemName = api.getProblemName(problemId);
						homework.addProblem(problemId, problemName);
						//�߰� �Ϸ� �ߴٴ� �޽��� 
					} catch (Exception e) {
						//api ȣ�� ����, ��õ� �޽��� 
					}
    			}
    		}
    		
    		else if(order != -1 && remove != null) {
    			//order������ ������ �����ϴ� �ڵ�

				if(order < 0 || order > homework.getCount()) {
					//�������� �ʴ� ������ �޽��� 
					return;
				}
    			Pair<Integer, String> removedValues = homework.removeProblem(order);
    			userManager.allUsersRemoveProblem(week, order);
    			
    			//���� ���õ� ���� �����ߴٴ� �޽��� 
    		}
    		
    		else if(activation != null) {
    			//������ ������ Ȱ��ȭ/��Ȱ��ȭ �ϴ� �ڵ�
    			boolean active;
    			if(homework.isActive() == (active = activation.equalsIgnoreCase("active"))) {
    				//�̹� Ȱ��ȭ / ��Ȱ��ȭ �Ǿ� �ִٴ� �޽��� 
    			}
    			
    			homework.setActive(active);
    			//Ȱ��ȭ / ��Ȱ��ȭ�� �ٲپ��ٴ� �޽���
    		}
    		
    		else if(problemId == -1 && order == -1 && activation == null && remove == null){
    			//������ ������ ��൵�� �����ִ� �޽���
    		}
    		
    		else {
    			//�߸��� ��ɾ� �Է��ߴٴ� �޽���
    		}
    		
    		MessageEmbed message = getEmbedMessage("", RGBA(34,177,16,0));
    		event.replyEmbeds(message).setEphemeral(true).queue();
    	}
    	else {
    		MessageEmbed message = getEmbedMessage("����� �����ڰ� �ƴմϴ�.", RGBA(255,0,0,0));
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
