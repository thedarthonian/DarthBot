package me.darth.darthbot.db;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class ViewProfile extends ListenerAdapter {
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
         if (e.getAuthor().isBot() && !e.getAuthor().equals(e.getJDA().getSelfUser())|| e.getAuthor().isFake()) {
			return;
		}
		String[] args = e.getMessage().getContentRaw().split(" ");
		if (args[0].equalsIgnoreCase("!profile") || args[0].equalsIgnoreCase("!account") || args[0].equalsIgnoreCase("!p")
			|| args[0].equalsIgnoreCase("!balance") || args[0].equalsIgnoreCase("!bal") || args[0].equalsIgnoreCase("!rank")
			|| args[0].equalsIgnoreCase("!level")) {
			Member target = null;
			try {
				if (!e.getMessage().getMentionedMembers().isEmpty()) {
					target = e.getMessage().getMentionedMembers().get(0);
				} else {
					target = me.darth.darthbot.main.Main.findUser(e.getMessage().getContentRaw().replace(args[0]+" ", ""), e.getGuild());
				}
			} catch (ArrayIndexOutOfBoundsException e1) {
				target = e.getMember();
			}
			if (args.length < 2) {
				target = e.getMember();
			}
			if (target == null) {
				e.getChannel().sendMessage(":no_entry: User not found!").queue();
				return;
			}
			try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/DarthBot", "root", "a8fc6c25d5c155c39f26f61def5376b0")) {

				long xp = 0L;
				int level = 0;
				ResultSet msgs = con.createStatement().executeQuery("SELECT * FROM GuildProfiles");
			      while (msgs.next()) {
				      long UserID = msgs.getLong("UserID");
				      long GuildID = msgs.getLong("GuildID");
				      long rsxp = msgs.getLong("xp");
				      int rslevel = msgs.getInt("Level");
				      if (UserID == target.getUser().getIdLong() && GuildID == e.getGuild().getIdLong()) {
				    	  xp = rsxp;
				    	  level = rslevel;
				      }
			      }
				
			      ResultSet rs = con.createStatement().executeQuery("SELECT * FROM profiles");
			      boolean found = false;
			      EmbedBuilder eb = new EmbedBuilder(me.darth.darthbot.main.Main.affiliation(target));
			      while (rs.next()) {
				      long UserID = rs.getLong("UserID");
				      long DBux = rs.getLong("DBux");
				      if (UserID == target.getUser().getIdLong()) {
				    	  found = true;
				    	  Member m = e.getGuild().getMemberById(UserID);
				    	  eb.setAuthor(target.getEffectiveName()+"'s Profile", null, target.getUser().getEffectiveAvatarUrl());
				    	  eb.setThumbnail(target.getUser().getEffectiveAvatarUrl());
				    	  eb.addField("User", target.getAsMention(), true);
				    	  List<Role> rolesRaw = m.getRoles();
				    	  List<String> roles = new ArrayList<String>();
				    	  int n2 = rolesRaw.size();
				    	  while (n2 > 0) {
				    		  n2 = n2 - 1;
				    		  Role r = rolesRaw.get(n2);
				    		  String mention = r.getAsMention();
								if (!r.equals(e.getGuild().getRoleById("560560525998817300"))) {
									roles.add(mention);
								}
				    	  }
				    	  Collections.reverse(roles);
				    	  if (!m.getRoles().isEmpty()) {
				    		  eb.setColor(m.getRoles().get(0).getColorRaw());
				    	  }
				    	  int reqxp = (level + 1) * 100;
					    	 eb.addField("Chat Level", "Level "+level+" *("+xp+"/"+reqxp+"*xp*)*", true);
				    	  if (DBux == -1337) {
				    		  eb.addField("DBux $$$", "$**"+Character.toString('\u221E')+"**", true);
				    	  } else {
				    		  String formattedbux = new DecimalFormat("#,###").format(DBux);
				    		  eb.addField("DBux $$$", "**$"+formattedbux+"**", true);
				    	  }
				    	  
				    	  ResultSet clans = con.createStatement().executeQuery("SELECT * FROM Clans");
				    	  String clan = "*No Clan*";
				    	  while (clans.next()) {
				    		  if (clans.getString("Members").contains(","+target.getUser().getId())) {
				    			  clan = "**"+clans.getString("Name")+"**";
				    		  }
				    	  }
				    	  eb.addField("Clan", "⚔"+clan+"⚔", true);
				    	  ResultSet inv = con.createStatement().executeQuery("SELECT * FROM profiles WHERE UserID = "+target.getUser().getId());
				    	  	while (inv.next()) {
								String invstring = "";
								String invraw = inv.getString("Inventory");
								if (invraw == null || invraw.replace(" ", "").isEmpty()) {
									eb.addField("Inventory", target.getEffectiveName()+"'s Inventory is Empty!", false);
								} else {
									String[] invsplit = invraw.split(",");
									for (int x = 0 ; x < invsplit.length ; x++) {
										if (!invsplit[x].isEmpty()) {
											ResultSet item = con.createStatement().executeQuery("SELECT * FROM StoreItems WHERE ID = "+invsplit[x]);
											while (item.next()) {
												invstring=invstring+"\n**"+item.getString("Name")+"**";
											}
										}
									}
									eb.addField("Inventory", invstring, true);
								}
				    	  	}
				    	  inv.close();
				    	  PreparedStatement st = con.prepareStatement("UPDATE profiles SET Name = ? WHERE UserID = "+target.getUser().getIdLong());
				    	  st.setString(1, target.getEffectiveName());
				    	  st.executeUpdate();
				      }
			      }
			      
			      if (found) {
			    	  e.getChannel().sendMessage(eb.build()).queue();
			    	  return;
			      } else {
					  Message msg = e.getChannel().sendMessage("I didn't find a profile for that user, creating one now: `Status: Creating...`").complete();
					  String query = " INSERT INTO profiles (UserID, Name, DBux)"
					    + " values (?, ?, ?)";
					  java.sql.PreparedStatement s = con.prepareStatement(query);
					  int bux = 0;
					  s.setLong(1, target.getUser().getIdLong());
					  s.setString(2, target.getEffectiveName());
					  s.setInt(3, bux);
					  s.execute();
					  msg.editMessage("I didn't find a profile for that user, creating one now: `Status: Profile Created`").queue();
						
			      }
			      rs.close();
			     
			      con.close();
			
			} catch (SQLException e1) {
			    e1.printStackTrace();
			}
			
		}
	}
}
