package me.Untaini.DiscordBotForAlgorithm;
import java.util.Collections;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Main extends ListenerAdapter{

	public static void main(String[] args) throws LoginException {
		JDA jda = JDABuilder.createLight((String)JSONManager.getJSON("Token.json").get("token"), Collections.emptyList())
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
    	if(!UserManager.checkRegister(userId)) {
	    	UserManager.addId(event.getUser().getId(), event.getOption("id").getAsString());
	    	event.reply(new StringBuilder().append("성공적으로 ").append(event.getOption("id").getAsString()).append("를 등록하였습니다.").toString()).setEphemeral(true).queue();
    	}
    	else 
    		event.reply("이미 등록된 유저입니다.").setEphemeral(true).queue();
    }
    
    public void editId(SlashCommandInteractionEvent event) {
    	String userId = event.getUser().getId();
    	if(UserManager.checkRegister(userId)) {
    		String prevId = UserManager.getBeakjoonId(userId), nowId = event.getOption("id").getAsString();
    		UserManager.editId(userId, nowId);
    		event.reply(new StringBuilder().append("성공적으로 ").append(prevId).append("에서 ").append(nowId).append("로 변경하였습니다.").toString()).setEphemeral(true).queue();
    	}
    	else
    		event.reply("아직 등록되지 않은 유저입니다.").setEphemeral(true).queue();
    }

}
