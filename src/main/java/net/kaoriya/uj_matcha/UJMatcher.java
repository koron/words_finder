package net.kaoriya.uj_matcha;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UJMatcher {

    class WorkingArea implements FireHandler {
        String text;
        MatchHandler handler;
        int max;

        int len;
        int index;
        int foundCount;
        boolean terminated;

        void setup(String text, MatchHandler handler, int max) {
            this.text = text;
            this.handler = handler;
            this.max = max;

            this.len = this.text.length();
            this.index = 0;
            this.foundCount = 0;
            this.terminated = false;
        }

        void teardown() {
            this.text = null;
            this.handler = null;
        }

        boolean hasNext() {
            return this.index < this.len;
        }

        char next() {
            return this.text.charAt(this.index++);
        }

        public boolean fired(StateMachine src, Event event) {
            ++this.foundCount;
            if (this.handler.matched(UJMatcher.this, event.id,
                        this.text, this.index)
                    || (this.max > 0 && this.foundCount >= this.max))
            {
                this.terminated = true;
            }
            return this.terminated;
        }
    }

    boolean verbose = false;

    final WordsTable wordsTable;

    final StateMachine stateMachine;

    final WorkingArea work = new WorkingArea();

    UJMatcher(WordsTable wordsTable) {
        this.wordsTable = wordsTable;
        this.stateMachine = new StateMachine(wordsTable.index);
    }

    /**
     * Get word text for wordId.
     */
    public String getWord(int wordId) {
        Word w = this.wordsTable.getWord(wordId);
        return w != null ? w.text : null;
    }

    /**
     * Match with text.
     *
     * When matched callback with handler.
     *
     * @param handler Callback interface when found match.
     * @param max Max count of match, 0 for ALL.
     */
    public boolean match(String text, MatchHandler handler, int max) {
        // Set up a match.
        this.work.setup(text, handler, max);
        this.stateMachine.clear();
        this.stateMachine.verbose = this.verbose;
        this.stateMachine.fireHandler = this.work;

        boolean found = false;
        while (this.work.hasNext()) {
            char ch = this.work.next();
            List<Event> events = this.wordsTable.getEvents(ch);
            if (events == null) {
                this.stateMachine.clear();
                continue;
            }
            this.stateMachine.put(events);
            if (this.work.terminated) {
                break;
            }
        }

        // Clean up.
        this.stateMachine.fireHandler = null;
        this.work.teardown();

        return this.work.foundCount > 0;
    }

    /**
     * Match with text, for all matches.
     */
    public boolean match(String text, MatchHandler handler) {
        return match(text, handler, 0);
    }

    /**
     * Find matches as list of Match.
     */
    public List<Match> find(String text, int max) {
        final ArrayList<Match> found = new ArrayList<>();
        match(text, new MatchHandler() {
            public boolean matched(UJMatcher matcher, int wordId, String text,
                int index)
            {
                String w = matcher.getWord(wordId);
                found.add(new Match(w, index - w.length() + 1));
                return true;
            }
        }, max);
        return found;
    }

    /**
     * Find matches as list of Match.
     */
    public List<Match> find(String text) {
        return find(text, 0);
    }

    ////////////////////////////////////////////////////////////////////////
    // Static Methods

    /**
     * Create a new UJMatcher.
     */
    public static UJMatcher newMatcher(List<String> words) {
        WordsTable wordsTable = new WordsTable();
        wordsTable.addAll(words);
        wordsTable.finish();
        return new UJMatcher(wordsTable);
    }

    /**
     * Create a new UJMatcher.
     */
    public static UJMatcher newMatcher(String ...words) {
        return newMatcher(Arrays.asList(words));
    }

    /**
     * Create a new clone UJMatcher.
     */
    public static UJMatcher newMatcher(UJMatcher matcher) {
        return new UJMatcher(matcher.wordsTable);
    }
}
