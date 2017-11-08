package no.kantega.niagara.workshop.sim;

import fj.F;
import fj.Ord;
import fj.data.Set;

import java.util.Random;

public interface Generator<A> {
  A gen(Random rand);

  default Generator<A> or(Generator<A> other) {
    return orWeight(1, this, 1, other);
  }

  default <B> Generator<B> map(F<A, B> f) {
    return rand -> f.f(gen(rand));
  }

  default <B> Generator<B> bind(F<A, Generator<B>> f) {
    return rand -> f.f(gen(rand)).gen(rand);
  }

  static <A> Generator<A> oneIn(Set<A> list) {
    return rand -> {
      int n = rand.nextInt(list.size());
      return list.toList().drop(n).head();
    };
  }

  static <A> Generator<A> always(A value) {
    return rand -> value;
  }

  static <A> Generator<A> orWeight(
    int weight1, Generator<A> gen1,
    int weight2, Generator<A> gen2,
    int weight3, Generator<A> gen3) {
    if (weight1 == 0)
      return orWeight(weight2, gen2, weight3, gen3);
    else if (weight2 == 0)
      return orWeight(weight1, gen1, weight3, gen3);
    else if (weight3 == 0)
      return orWeight(weight1, gen1, weight2, gen2);


    return rand -> {
      double d = rand.nextDouble();
      return d < ((double) weight1) / (weight1 + weight2 + weight3) ? gen1.gen(rand) :
             d < ((double) (weight1 + weight2)) / (weight1 + weight2 + weight3) ? gen2.gen(rand) :
             gen3.gen(rand);
    };
  }

  static <A> Generator<A> orWeight(int weight1, Generator<A> gen1, int weight2, Generator<A> gen2) {
    if (weight1 == 0)
      return gen2;
    else if (weight2 == 0)
      return gen1;
    else
      return rand -> {
        boolean choose1 = rand.nextDouble() < ((double) weight1) / (weight1 + weight2);
        return choose1 ? gen1.gen(rand) : gen2.gen(rand);
      };
  }

  String para =
    "Spicy jalapeno bacon ipsum dolor amet leberkas t-bone salami " +
      "tri-tip ham hock.Chicken short ribs drumstick burgdoggen ribeye, " +
      "pork belly beef ribs filet mignon ball tip strip steak.Kevin alcatra turducken, sausage cow " +
      "burgdoggen andouille shank shankle shoulder flank jowl.Turducken meatloaf salami swine " +
      "prosciutto chuck corned beef frankfurter short ribs landjaeger, pastrami picanha.Prosciutto tongue pig " +
      "tenderloin, burgdoggen chuck spare ribs.Tri-tip landjaeger leberkas meatloaf porchetta t-bone corned beef pork " +
      "chop andouille pancetta tongue venison cupim pork belly.Ribeye t-bone tongue bresaola tail sirloin corned beef " +
      "fatback flank tenderloin tri-tip.Ground round meatloaf boudin kielbasa beef, prosciutto shoulder strip steak kevin." +
      "Chicken frankfurter ground round short ribs, bacon tri-tip swine burgdoggen andouille brisket." +
      "Tail tri-tip spare ribs, fatback shankle beef ribs capicola tenderloin." +
      "Cupim tenderloin bresaola beef ribs, pancetta meatball short ribs boudin chicken kielbasa spare ribs swine tri-tip pork belly brisket." +
      "Alcatra meatloaf pig pork belly jowl tenderloin." +
      "Ribeye corned beef jerky picanha boudin tri-tip ball tip." +
      "Shank sirloin bacon tenderloin biltong beef bresaola.";

  Set<String> sentences =
    Set.arraySet(Ord.stringOrd, para.split("\\."));

  Generator<String> randSentence =
    oneIn(sentences);

}
