load("crime3df.rdata")
load("foci.rdata")

cb.ints <- foci2intervals(cb.foci)
cv.ints <- foci2intervals(cv.foci)
nobias.df <- crime3.df[crime3.df$bias!="none",]
names(nobias.df)[1] <- "isolate"
names(nobias.df)[2] <- "capture"

pdf()

sb <- trellis.par.get("strip.background")
sb[["col"]][1] <- "lightblue"
trellis.par.set("strip.background", sb)

bwplot(isolate + capture ~ bias, data=nobias.df, 
	ylim=c(-1,1), 
	coef=0, 
	outer=T, 
	panel=function(x, y, ...){
		panel.hanoi(x, y, col="gray90", border="gray90", breaks=if(panel.number()==1){cv.ints}else{cb.ints}, ...);
		panel.bwplot(x, y, ...);
		panel.points(tapply(y, factor(x), FUN = mean), pch=20, cex=1, col="red");
	},
	par.settings=list(box.rectangle=list(col="blue"), box.umbrella=list(col="blue")),
	main=list(label="Summary of mean activations for two belief sets\nin 50 runs, time step 5000", fontface="bold"), # you can also use fontfamily, fontsize here
	xlab="biases\n\n(box: quartiles,  red: mean,  black: median,  gray: distribution)",
	ylab="per-run mean activations")

dev.off()


# here's the bwplot call in one line:
# bwplot(isolate + capture ~ bias, data=nobias.df, ylim=c(-1,1), coef=0, outer=T, panel=function(x, y, ...){panel.hanoi(x, y, col="gray90", border="gray90", breaks=if(panel.number()==1){cv.ints}else{cb.ints}, ...); panel.bwplot(x, y, ...); panel.points(tapply(y, factor(x), FUN = mean), pch=20, cex=1, col="red")}, par.settings=list(box.rectangle=list(col="blue"), box.umbrella=list(col="blue")), main=list(label="Summary of mean activations for two belief sets in 50 runs, time step 5000", fontface="bold"), xlab="biases\n\n(box: quartiles,  red: mean,  black: median,  gray: distribution)", ylab="per-run mean activations")
