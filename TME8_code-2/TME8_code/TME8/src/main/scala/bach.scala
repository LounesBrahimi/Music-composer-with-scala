package upmc.akka.culto

import math._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import akka.actor.{Props, Actor, ActorRef, ActorSystem}


abstract class ObjectMusical
abstract class SimpleObjectMusical () extends ObjectMusical
abstract class ComposeObjectMusical () extends ObjectMusical
case class Note (pitch:Int, dur:Int, vel:Int) extends SimpleObjectMusical
case class Rest (dur:Int) extends SimpleObjectMusical
case class Sequential (elements:List[ObjectMusical]) extends ComposeObjectMusical
case class Parallel (elements:List[ObjectMusical]) extends ComposeObjectMusical

case class MidiNote (pitch:Int, vel:Int, dur:Int, at:Int)
 
/////////////////////////////////////////////////

class BachActor extends Actor {

//////////////////////////////////////////////////
 val remote =
  context.actorSelection("akka.tcp://Player@127.0.0.1:6000/user/Instrument")

////////////////////////////////////////////////

def receive = {
    case "START" => {
      //println("start")
      //play(exemple)
     // play(canon(exemple, 100))
      //println(duration(exemple))
      //play(canon(repeat(exemple, 3),20))
      //play(voix1);
      //play(concat_mo(exemple, exemple))
     // play(voix1)
      //play(voix2)
      play(canon_Bach)
    // play(roiVoix1)
    }
       
}

/////////////////////////////////////////////////

//Question 1
// val exemple = ???

val exemple = Parallel ( List ( Sequential ( List ( Note (60 , 1000 , 100 ) , Note (64 , 500 , 100 ), Note (62 , 500 , 100 ),Rest (1000) , Note (68 , 1000 , 100 )) ),

Sequential ( List ( Note (52 , 2000 , 100 ) , Note (55 , 1000 , 100 ), Note (55 , 1000 , 100 )
           )
     )))

//Question 2
 
 // Calcule la duree d'un objet musical
  def duration (obj:ObjectMusical):Int =
  obj match {
    case Note(p,d,v) => d
    case Rest(d) => d
    case Sequential (l) => (l.map(duration)).foldLeft(0)(_+_)
    case Parallel (l) => (l.map(duration)).foldLeft(0)(math.max) 
  }


//Question 3
  def play (obj:ObjectMusical): Unit = {
    play_midi (obj, 0)
  }

  def send_a_note (p:Int, d:Int, v:Int, at:Int): Unit = {
    remote ! MidiNote(p,v,d,at)
  }

 
  def play_midi (obj:ObjectMusical, at:Int): Unit =
  obj match {
    case Note(p,d,v) => send_a_note (p,d,v,at)
    case Rest(d) => Nil
    case Sequential (l) => {var date = at
                        l.foreach(n=>{play_midi(n,date); date = date + duration(n)})}
    case Parallel (l) => l.foreach(n=>play_midi(n,at))
  }
 

 // Copy un objet musical
  def copy (obj:ObjectMusical):ObjectMusical =
  obj match {
    case Note(p,d,v) => Note(p,d,v)
    case Rest(d) => Rest(d)
    case Sequential (l) => Sequential (l.map(copy))
    case Parallel (l) => Parallel (l.map(copy))
  }

  // Compte le nombre de notes d'un objet musical
  def note_count (obj:ObjectMusical):Int =
  obj match {
    case Note(p,d,v) => 1
    case Parallel (l) => (l.map(note_count)).foldLeft(0)(_+_)
    case Sequential (l) => (l.map(note_count)).foldLeft(0)(_+_)
    case _ => 0
  }
 
  // Strech un objet musical par un factor fact
  def stretch (obj:ObjectMusical, fact:Double ):ObjectMusical =
  obj match {
    case Note(p,d,v) => Note(p,(d*fact).toInt,v)
    case Rest(d) => Rest((d*fact).toInt)
    case Parallel (l) => Parallel (l.map(stretch (_,fact)))
    case Sequential (l) => Sequential (l.map(stretch (_,fact)))
  }
 

// Question 4
 
// Transpose obj de n demitons
  def transpose (obj:ObjectMusical, n:Int ):ObjectMusical =
obj match {
  case Note(p,d,v) => Note(p+n,d,v)
  case Rest(d) => Rest(d)
  case Parallel (l) => Parallel (l.map(transpose (_,n)))
  case Sequential (l) => Sequential (l.map(transpose (_,n)))
}

// mirror de obj au tour du center c
  def mirror (obj:ObjectMusical, c:Int ):ObjectMusical =
obj match {
  case Note(p,d,v) => Note(c - (p - c),d,v)
  case Rest(d) => Rest(d)
  case Parallel (l) => Parallel (l.map(mirror (_,c)))
  case Sequential (l) => Sequential (l.map(mirror (_,c)))
}

// retrograde un obj 
  def retrograde (obj:ObjectMusical):ObjectMusical =
obj match {
  case Sequential (l) => Sequential (l.reverse.map(retrograde))
  case o => o
}

// repeat
  def repeat (obj:ObjectMusical, n:Int):ObjectMusical =
(obj, n) match {
  case (_, 0) => obj
  case (Note(p,d,v), _) => Sequential (List(Note(p,d,v), repeat(Note(p,d,v), (n-1))))
  case (Rest(d),_) => Sequential (List(Rest(d), repeat(Rest(d), (n-1))))
  case (Parallel(l), _) => Sequential (List(Parallel (l), repeat(Parallel (l), (n-1))))
  case (Sequential(l), _) => Sequential (List(Sequential (l), repeat(Sequential (l), (n-1))))
}

//canon
  def canon(obj:ObjectMusical, n:Int):ObjectMusical =
(obj, n) match {
    case (o, 1) => Parallel ( List (o, o))
    case (Note(p,d,v), n) => Parallel (List(Note(p,d,v), Sequential(List(Rest(n), Note(p,d,v)))))
    case (Rest(d),n) => Parallel (List(Rest(d), Sequential (List(Rest(n), Rest(d)))))
    case (Parallel(l), n) => Parallel (List(Parallel(l), Sequential(List(Rest(n), Parallel(l)))))
    case (Sequential(l), n) => Parallel (List(Sequential(l), Sequential(List(Rest(n),     Sequential(l)))))
}

//concat_mo
  def concat_mo(obj1:ObjectMusical, obj2:ObjectMusical):ObjectMusical =
    Sequential(List(obj1, obj2))

    val exemple2 =  canon( repeat( exemple, 3), 1000)


//Question 5

//Question 5 BACH
 val voix1 = Sequential ( List (
  Note (60 , 750 , 106 ) , Note (62 , 250 , 108 ) , Note (63 , 250 , 108 ) ,
  Note (64 , 250 , 109 ) , Note (65 , 250 , 109 ) , Note (66 , 250 , 110 ) ,
  Note (67 , 1000 , 110 ) , Note (68 , 625 , 113 ) ,Note (65 , 125 , 114 ) ,
  Note (61 , 125 , 112 ) , Note (60 , 125 , 112 ) , Note (59 , 500 , 112 ) ,
  Rest (500) , Rest (500) , Note (67 , 1000 , 109 ) ,Note (66 , 1000 , 108 ) ,
  Note (65 , 1000 , 106 ) , Note (64 , 1000 , 106 ) , Note (63 , 1000 , 106 ) ,
  Note (62 , 750 , 106 ) , Note (61 , 250 , 106 ) , Note (58 , 250 , 106 ) ,
  Note (57 , 250 , 106 ) , Note (62 , 500 , 106 ) ,Rest (1000),
  Note (67 , 1000 , 106 ) , Note (65 , 500 , 106 ) , Note (64 , 1000 , 106 )))

val voix2 = Sequential (List (
  Rest (125) , Note (48 , 125 , 100 ), Note (51 , 125 , 100 ),
  Note (55 , 125 , 100 ),Note (60 , 1000 , 100 ),Note (58 , 250 , 100 ),
  Note (57 , 250 , 100 ),Note (58 , 625 , 100 ),Note (52 , 125 , 100 ),
  Note (50 , 125 , 100 ),Note (52 , 125 , 100 ),Note (53 , 125 , 100 ),
  Note (48 , 125 , 100 ),Note (53 , 125 , 100 ),Note (55 , 125 , 100 ),
  Note (56 , 750 , 100 ),Note (56 , 250 , 100 ),Note (55 , 250 , 100 ),
  Note (53 , 250 , 100 ),Note (51 , 625 , 100 ),Note (51 , 125 , 100 ),
  Note (53 , 125 , 100 ),Note (51 , 125 , 100 ),Note (50 , 250 , 100 ),
  Note (48 , 250 , 100 ),Note (49 , 500 , 100 ), Rest (250) ,Note (50 , 250 , 100 ),
  Note (51 , 250 , 100 ),Note (50 , 250 , 100 ),Note (48 , 125 , 100 ),
  Note (47 , 125 , 100 ),Note (48 , 125 , 100 ),Note (47 , 125 , 100 ),
  Note (48 , 125 , 100 ),Note (50 , 125 , 100 ),Note (48 , 125 , 100 ),
  Note (46 , 125 , 100 ),Note (45 , 125 , 100 ),Note (43 , 125 , 100 ),
  Note (45 , 125 , 100 ),Note (46 , 125 , 100 ),Note (48 , 125 , 100 ),
  Note (45 , 125 , 100 ),Note (46 , 125 , 100 ),Note (48 , 125 , 100 ),
  Note (50 , 250 , 100 ),Note (60 , 500 , 100 ),Note (58 , 125 , 100 ),
  Note (57 , 125 , 100 ),Note (58 , 250 , 100 ),Note (55 , 250 , 100 ),
  Note (52 , 250 , 100 ),Note (57 , 125 , 100 ),Note (55 , 125 , 100 ),
  Note (54 , 250 , 100 ),Note (55 , 125 , 100 ),Note (57 , 125 , 100 ),
  Note (58 , 250 , 100 ), Note (49 , 250 , 100 ),Note (50 , 500 , 100 ),
  Rest (500) , Rest (250) , Note (50 , 375 , 100 ),Note (53 , 125 , 100 ),
  Note (52 , 125 , 100 ),
  Note (50 , 125 , 100 ),Note (49 , 125 , 100 ),Note (50 , 125 , 100 ),
  Note (52 , 125 , 100 ),Note (53 , 125 , 100 ),Note (55 , 125 , 100 ),
  Note (58 , 125 , 100 ),Note (57 , 125 , 100 ),Note (55 , 125 , 100 )))

val roiVoix1 = Sequential (List (voix1, transpose(voix1 , 2), transpose(transpose(voix1 , 2) , 2), 
                                  transpose(transpose(transpose(voix1 , 2) , 2) , 2), 
                                  transpose(transpose(transpose(transpose(voix1 , 2) , 2) , 2) , 2), 
                                  transpose(transpose(transpose(transpose(transpose(voix1 , 2) , 2) , 2) , 2) , 2)));
val deuxiemeVoix = Sequential ( List (voix2, transpose(voix1 , 1), transpose(transpose(voix1 , 1) , 1), 
                                        transpose(transpose(transpose(voix1 , 1) , 1) , 1), 
                                          transpose(transpose(transpose(voix1 , 1) , 1) , 1), 
                                            transpose(transpose(transpose(voix1 , 1) , 1) , 1)));
val mesureVoix2 = Sequential ( List (Rest (2000), transpose(deuxiemeVoix, 7)));

def canon_Bach ():ObjectMusical = {
    Parallel ( List (
                       roiVoix1,
                       deuxiemeVoix,
                       mesureVoix2

    ))
  }
}
//////////////////////////////////////////////////


/**************** MAin object *******************/
object bach extends App {
  val system = ActorSystem("Bach")
  val localActor = system.actorOf(Props[BachActor], name = "Bach")
  localActor ! "START"
}