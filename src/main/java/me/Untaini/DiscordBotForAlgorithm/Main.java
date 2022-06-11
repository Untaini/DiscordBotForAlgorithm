package me.Untaini.DiscordBotForAlgorithm;
import java.awt.Color;
import java.util.Collections;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Main extends ListenerAdapter{

	public static void main(String[] args) throws LoginException {
		JDA jda = JDABuilder.createLight((String)JSONManager.getJSONFile("token.json").get("token"), Collections.emptyList())
				  .addEventListeners(new Main())
		          .build();
		
		jda.updateCommands()
			.addCommands(Commands.slash("register-id", "register Baekjoon Id").addOption(OptionType.STRING, "id", "Baekjoon Id"))
			.addCommands(Commands.slash("edit-id", "edit Baekjoon Id").addOption(OptionType.STRING, "id", "Baekjoon Id"))
			.queue();
	}
	

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {
    	switch(event.getName()) {
    	case "register-id" -> registerId(event);
    	case "edit-id" -> editId(event);
    	default -> event.reply("unknown command").setEphemeral(true).queue();
    	}
    }
    
    public void registerId(SlashCommandInteractionEvent event) {
    	String userId = event.getUser().getId();
    	UserManager userManager = new UserManager();
    	if(!userManager.checkRegister(userId)) {
	    	userManager.addId(event.getUser().getId(), event.getOption("id").getAsString());
    		MessageEmbed message = getEmbedMessage(new StringBuilder().append("성공적으로 ").append(event.getOption("id").getAsString()).append("를 등록하였습니다.").toString(), RGBA(34,177,16,0));
    		event.replyEmbeds(message).setEphemeral(true).queue();
    	}
    	else {
			MessageEmbed message = getEmbedMessage("당신은 이미 등록된 유저입니다.", RGBA(255,0,0,0));
			event.replyEmbeds(message).setEphemeral(true).queue();
    	}
    }
    
    public void editId(SlashCommandInteractionEvent event) {
    	String userId = event.getUser().getId();
    	UserManager userManager = new UserManager();
    	if(userManager.checkRegister(userId)) {
    		String prevId = userManager.getBeakjoonId(userId), nowId = event.getOption("id").getAsString();
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
    
    private int RGBA(int red, int blue, int green, int alpha) {
    	Color rgb = new Color(red, blue, green, alpha);
    	return rgb.getRGB();
    }
    
    private MessageEmbed getEmbedMessage(String description, int color) {
    	MessageEmbed message = new MessageEmbed(null, null, description, EmbedType.RICH, null, color, null, null, null, null, null, null, null);
    	return message;
    	
    }

}
