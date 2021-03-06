package me.darth.darthbot.commands;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class Slots extends ListenerAdapter {

	public String getSlot(User m) {
		int rand = new Random().nextInt(10) + 1;
		if (rand == 1 || rand == 2) {
			return ":cherries:";
		} else if (rand == 3 || rand == 4) {
			return ":apple:";
		}  else if (rand == 5 || rand == 6) {
			return ":lemon:";
		}  else if (rand == 7 || rand == 8) {
			return ":pineapple:";
		}  else if (rand == 9 || rand == 10) {
			return ":watermelon:";
		} else {
			return null;
		}
	}
	
	@Override
	public synchronized void onGuildMessageReceived(GuildMessageReceivedEvent e) {
         if (e.getAuthor().isBot() && !e.getAuthor().equals(e.getJDA().getSelfUser())|| e.getAuthor().isFake()) {
			return;
		}
		String[] args = e.getMessage().getContentRaw().split(" ");
		if (args[0].equalsIgnoreCase("!emojis") && e.getAuthor().getId().equals("159770472567799808")) {
			e.getChannel().sendMessage(e.getGuild().getEmotes().toString()).queue();
		}
		if (args[0].equalsIgnoreCase("!slots") || args[0].equalsIgnoreCase("!slot") || args[0].equalsIgnoreCase("!bet")) {
			
			if (args.length < 2) {
				e.getChannel().sendMessage(":no_entry: Invalid Syntax: `!slots <Money>`").queue();
				return;
			}
			int tobet = -1;
			try {
				tobet = Integer.parseInt(args[1]);
			} catch (NumberFormatException e1) {
				e.getChannel().sendMessage(":no_entry: Invalid Syntax: `!slots <Money>`").queue();
				return;
			}
			
			if (tobet <= 0) {
				e.getChannel().sendMessage(":no_entry: You must bet more than **$0**!").queue();
				return;
			}
			try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/DarthBot", "root", "a8fc6c25d5c155c39f26f61def5376b0")) {
			    ResultSet rs = con.createStatement().executeQuery("SELECT * FROM profiles WHERE UserID = "+e.getAuthor().getIdLong());
			    while (rs.next()) {
			    	if (tobet > rs.getLong("DBux")) {
			    		e.getChannel().sendMessage(":no_entry: You can't bet more than your balance! You can only bet **$"+new DecimalFormat("#,###").format(rs.getLong("DBux"))+"**!").queue();
			    		return;
			    	}
					EmbedBuilder eb = new EmbedBuilder().setAuthor("🍒 Fruit Machine 🍋", null, e.getAuthor().getEffectiveAvatarUrl()).setColor(Color.yellow)
							.setDescription("Betting: `$"+new DecimalFormat("#,###").format(tobet)+"`");
					String slotgif = me.darth.darthbot.main.Main.sm.getGuildById("568849490425937940").getEmoteById("599953101050609664").getAsMention();
					
					String[] slot = (getSlot(e.getAuthor())+","+slotgif+","+slotgif).split(",");
					eb.addField("🎰 Slots 🎰",slot[0]+" "+slot[1]+" "+slot[2], false);
					Message msg = e.getChannel().sendMessage(eb.build()).complete();
					MessageEmbed u2a = null;
					for (int x = 1 ; x < 3 ; x++) {

						eb.getFields().clear();
						slot[x] = getSlot(e.getAuthor());
						eb.addField("🎰 Slots 🎰",slot[0]+" "+slot[1]+" "+slot[2], false);
						if (x != 2) {
							u2a=eb.build();
						}
					}
					final MessageEmbed u2 = u2a;
					ScheduledExecutorService executorService
				      = Executors.newSingleThreadScheduledExecutor();
					ScheduledFuture<?> scheduledFuture = executorService.schedule(() -> {
						msg.editMessage(u2).queue();
				    }, 1, TimeUnit.SECONDS);
					long newbal = -1;
					boolean log = false;
					if (slot[0].equals(slot[1]) && slot[1].equals(slot[2]) && slot[0].equals(slot[2])) {
						int winnings = tobet * 2;
						int totalwin = winnings + tobet;
						log = true;
						newbal = rs.getLong("DBux") + winnings;
						con.prepareStatement("UPDATE profiles SET DBux = "+newbal+" WHERE UserID = "+e.getAuthor().getId()).execute();
						eb.addField("TRIPLE WIN", "**YOU WIN** `$"+new DecimalFormat("#,###").format(winnings)+"`", false).setColor(Color.green).setDescription("Starting Bet: `$"+
								new DecimalFormat("#,###").format(tobet)+"`\nNew Balance: `$"+new DecimalFormat("#,###").format(newbal)+"`");
					} else if (slot[0].equals(slot[1]) || slot[0].equals(slot[2]) || slot[1].equals(slot[2])) {
						int winnings = tobet;
						int totalwin = winnings + tobet;
						log = true;
						newbal = rs.getLong("DBux") + winnings;
						con.prepareStatement("UPDATE profiles SET DBux = "+newbal+" WHERE UserID = "+e.getAuthor().getId()).execute();
						eb.addField("DOUBLE WIN", "**TOTAL WIN** `$"+new DecimalFormat("#,###").format(winnings)+"`", false).setColor(Color.green).setDescription("Starting Bet: `$"+
								new DecimalFormat("#,###").format(tobet)+"`\nNew Balance: `$"+new DecimalFormat("#,###").format(newbal)+"`");
					} else {
						newbal = rs.getLong("DBux") - tobet;
						con.prepareStatement("UPDATE profiles SET DBux = "+newbal+" WHERE UserID = "+e.getAuthor().getId()).execute();
						eb.setColor(Color.red).setDescription("Lost: `$"+new DecimalFormat("#,###").format(tobet)+"`\nNew Balance: `$"
								+new DecimalFormat("#,###").format(newbal)+"`");
					}
					final MessageEmbed u3 = eb.build();
					final boolean logf = log;
					ScheduledFuture<?> scheduledFuture2 = executorService.schedule(() -> {
						msg.editMessage(u3).queue();
						if (logf) {
							eb.setFooter(e.getGuild().toString(), e.getGuild().getIconUrl());
							eb.addField("User Details", e.getMember().getAsMention()+" ("+e.getMember().getUser().toString()+")", false).setTimestamp(Instant.from(ZonedDateTime.now()));
							
							me.darth.darthbot.main.Main.sm.getTextChannelById("590156048002711552").sendMessage(eb.build()).queue();
						}
				    }, 2, TimeUnit.SECONDS);
			    }
			} catch (SQLException e1) {e1.printStackTrace();}
		}
		
	}

}
