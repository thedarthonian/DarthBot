package me.darth.darthbot.moderation;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class History extends ListenerAdapter {

	@SuppressWarnings("deprecation")
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
		if (e.getGuild().getId().equals("393499439739961366")) {
			return;
		}
		String[] args = e.getMessage().getContentRaw().split(" ");
		if (args[0].equalsIgnoreCase("!history") || args[0].equalsIgnoreCase("!his")) {
			try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/DarthBot", "root", "a8fc6c25d5c155c39f26f61def5376b0")) {
			      ResultSet rs = con.createStatement().executeQuery("SELECT * FROM GuildInfo WHERE GuildID = "+e.getGuild().getIdLong());
			      while (rs.next())
			      {
			        long ModRoleID = rs.getLong("Moderator");
			        if (!e.getMember().getRoles().contains(e.getGuild().getRoleById(ModRoleID))) {
			        	MessageEmbed eb = new EmbedBuilder().setDescription("⛔ "+e.getMember().getAsMention()+", you must have the "+e.getGuild().getRoleById(ModRoleID).getAsMention()
			        			+ " role to use that command!").setColor(Color.red).build();
			        	e.getChannel().sendMessage(eb).queue();
			        	return;
			        }
			      }
			      rs.close();
			      con.close();
			} catch (SQLException e1) {
			   e1.printStackTrace();
			}
			Member target = null;
			if (args.length < 2) {
				target = e.getMember();
			} else if (!e.getMessage().getMentionedMembers().isEmpty()) {
				target = e.getMessage().getMentionedMembers().get(0);
			} else {
				target = me.darth.darthbot.main.Main.findUser(e.getMessage().getContentRaw().replace(args[0]+" ", ""), e.getGuild());
			}
			
			if (target == null) {
				e.getChannel().sendMessage(":no_entry: Member not found!").queue();
				return;
			}
			try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/DarthBot", "root", "a8fc6c25d5c155c39f26f61def5376b0")) {
			      ResultSet rs = con.createStatement().executeQuery("SELECT * FROM ModHistory WHERE PunishedID = "+target.getUser().getIdLong()+" AND GuildID = "+e.getGuild().getIdLong()+" ORDER BY `TimeStamp` DESC");
			      EmbedBuilder eb = new EmbedBuilder().setColor(Color.red);
			      int counter = 0;
			      while (rs.next()) {
			    	  if (counter < 10) {
				    	  try {
					    	  String type = rs.getString("Type");
					    	  Calendar cal = Calendar.getInstance();
					    	  cal.setTimeInMillis(rs.getLong("TimeStamp"));
					    	  String reason = rs.getString("Reason");
					    	  String punisher = e.getGuild().getMemberById(rs.getLong("PunisherID")).getEffectiveName();
					    	  String ft = cal.getTime().getDate()+"/"+(cal.getTime().getMonth() < 10 ? "0" : "") + Math.addExact(cal.getTime().getMonth(), 1)+"/"+Math.subtractExact(cal.getTime().getYear(), 100)+" @ "+cal.getTime().getHours()+":"+(cal.getTime().getMinutes() < 10 ? "0" : "") + cal.getTime().getMinutes();
					    	  if (type.equals("BAN")) {
					    		  eb.addField("Banned on "+ft, "**Reason:** `"+reason+"` **by "+punisher+"**", false);
					    	  } else if (type.equals("KICK")) {
					    		  eb.addField("Kicked on "+ft, "**Reason:** `"+reason+"` **by "+punisher+"**", false);
					    	  } else if (type.equals("TEMPBAN")) {
					    		  Calendar exp = Calendar.getInstance();
					    		  exp.setTimeInMillis(rs.getLong("Expires"));
					    		  boolean active = true;
					    		  if (new Date().getTime() > exp.getTimeInMillis()) {
					    			  active=false;
					    		  }
					    		  eb.addField("Temporarily Banned on "+ft+" (Active: "+active+")", "**Reason:** `"+reason+"` **by "+punisher+"**\n**Expires:** "+exp.getTime(), false);
					    	  } else if (type.equals("TEMPMUTE")) {
					    		  Calendar exp = Calendar.getInstance();
					    		  exp.setTimeInMillis(rs.getLong("Expires"));
					    		  boolean active = true;
					    		  if (new Date().getTime() > exp.getTimeInMillis()) {
					    			  active=false;
					    		  }
					    		  eb.addField("Temporarily Muted on "+ft+" (Active: "+active+")", "**Reason:** `"+reason+"` **by "+punisher+"**\n**Expires:** "+exp.getTime(), false);
					    	  } else if (type.equals("WARN")) {
					    		  eb.addField("Warned on "+ft, "**Reason:** `"+reason+"` **by "+punisher+"**", false);
					    	  } else {
					    		  eb.addField(type+" on "+ft, "**Reason:** `"+reason+"` **by "+punisher+"**", false);
					    	  }
				    	  } catch (NullPointerException e1) {}
			    	  }
			    	  counter++;
			      }
			      eb.setAuthor(target.getEffectiveName()+"'s Punishment History ("+eb.getFields().size()+"/"+counter+")", null, target.getUser().getEffectiveAvatarUrl());
			      if (eb.getFields().size() == 0) {
			    	 eb.setTitle(":angel: No punishments found!");
			    	 eb.setColor(Color.green);
			      }
			      e.getChannel().sendMessage(eb.build()).queue();
			      rs.close();
			      con.close();
			} catch (SQLException e1) {
			   e1.printStackTrace();
			}
		}
		
	}
}
