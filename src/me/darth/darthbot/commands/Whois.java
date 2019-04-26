package me.darth.darthbot.commands;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class Whois extends ListenerAdapter {

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
		String[] args = e.getMessage().getContentRaw().split(" ");
		Member target = e.getMember();
		if (args[0].equalsIgnoreCase("!whois")) {
			try {
				if (!e.getMessage().getMentionedMembers().isEmpty()) {
					target = e.getMessage().getMentionedMembers().get(0);
				} else {
					target = me.darth.darthbot.main.Main.findUser(args[1]);
					if (target == null) {
						target = e.getMember();
					}
				}
			} catch (ArrayIndexOutOfBoundsException e1) {target=e.getMember();}
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(Color.BLUE);
			eb.setAuthor("Information about "+target.getEffectiveName(),null,target.getUser().getAvatarUrl());
			eb.addField("User", ""+target.getAsMention(), true);
			if (target.getNickname() == null) {
				eb.addField("Nickname", "None", true);
			} else {
				eb.addField("Nickname", ""+target.getNickname(), true);
			}
			if (target.getGame() == null) {
				eb.addField("Currently Playing", "Nothing", true);
			} else {
				eb.addField("Currently Playing", ""+target.getGame().getName(), true);
			}
			int hour = -1;
			int minute = -1;
			if (target.getJoinDate().getHour() < 10) {
				hour = Integer.parseInt("0"+target.getJoinDate().getHour());
			} else {
				hour = target.getJoinDate().getHour();
			}
			if (target.getJoinDate().getMinute() < 10) {
				minute = Integer.parseInt("0"+target.getJoinDate().getMinute());
			} else {
				minute = target.getJoinDate().getMinute();
			}
			eb.addField("Join Date", ""+target.getJoinDate().getDayOfMonth()+"/"+target.getJoinDate().getMonthValue()
				+"/"+target.getJoinDate().getYear()+" @ "+hour+":"+minute, true);
			List<Role> rolesRaw = target.getRoles();
			List<String> roles = new ArrayList<String>();
			int n2 = rolesRaw.size();
			while (n2 > 0) {
				n2 = n2 - 1;
				Role r = rolesRaw.get(n2);
				String mention = r.getAsMention();
				roles.add(mention);
			}
			Collections.reverse(roles);
			eb.addField("Roles ["+roles.size()+"]", ""+roles, true);
			eb.setThumbnail(target.getUser().getAvatarUrl());
			
			List<Permission> permsRaw = target.getPermissions();
			
			List<String> perms = new ArrayList<String>();
			int n = permsRaw.size();
			while (n > 0) {
				n = n - 1;
				Permission p = permsRaw.get(n);
				if (!p.equals(Permission.CREATE_INSTANT_INVITE) && !p.equals(Permission.MESSAGE_ADD_REACTION) && 
					!p.equals(Permission.MESSAGE_ATTACH_FILES) && !p.equals(Permission.MESSAGE_EMBED_LINKS) && 
					!p.equals(Permission.MESSAGE_EXT_EMOJI) && !p.equals(Permission.MESSAGE_HISTORY) && 
					!p.equals(Permission.MESSAGE_READ) && !p.equals(Permission.MESSAGE_TTS) && !p.equals(Permission.MESSAGE_WRITE) && 
					!p.equals(Permission.VIEW_CHANNEL) && !p.equals(Permission.VOICE_CONNECT) && !p.equals(Permission.VOICE_SPEAK) &&
					!p.equals(Permission.VOICE_USE_VAD)) {
						perms.add(p.getName());
				}
			}
			Collections.reverse(perms);
			String permsstring = perms.toString();
			if (target.getRoles().contains(e.getGuild().getRoleById("399332438079176715")) && !target.getRoles().contains(e.getGuild().getRoleById("393796810918985728"))) {
				permsstring = permsstring+" , Kick Members, Ban Members";
			}
			eb.addField("Permissions ["+perms.size()+"]", ""+permsstring, true);
			if (me.darth.darthbot.main.Main.jda.getGuildById("568849490425937940").isMember(target.getUser())) {
				eb.setFooter(target.getUser().getName()+" is a Member of the DarthBot Discord!", "https://i.imgur.com/2Dm5fiE.png");
				if (me.darth.darthbot.main.Main.jda.getGuildById("568849490425937940").getMember(target.getUser()).getRoles()
				.contains(me.darth.darthbot.main.Main.jda.getGuildById("568849490425937940").getRoleById("569464005416976394"))) {
					eb.setFooter(target.getUser().getName()+" is a Server Moderator on the DarthBot Discord!", "https://i.imgur.com/xSA1FBS.png");
				}
				if (me.darth.darthbot.main.Main.jda.getGuildById("568849490425937940").getMember(target.getUser()).getRoles()
				.contains(me.darth.darthbot.main.Main.jda.getGuildById("568849490425937940").getRoleById("569463842552152094"))) {
					eb.setFooter(target.getUser().getName()+" is a Developer of DarthBot!", "https://i.imgur.com/zDmrbWS.png");
				}
			}
			
			eb.setTimestamp(Instant.from(ZonedDateTime.now()));
			e.getChannel().sendMessage(eb.build()).queue();
			
		}
	}

}