package com.example.tournafy.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.tournafy.R;
import com.example.tournafy.domain.models.tournament.TournamentMatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom view for rendering tournament brackets.
 * Displays knockout stage matches in a tournament tree structure.
 */
public class BracketView extends View {

    // Constants
    private static final int MATCH_BOX_WIDTH = 180;
    private static final int MATCH_BOX_HEIGHT = 80;
    private static final int VERTICAL_SPACING = 40;
    private static final int HORIZONTAL_SPACING = 100;
    private static final int TEXT_SIZE_TEAM = 14;
    private static final int TEXT_SIZE_SCORE = 12;
    private static final int CORNER_RADIUS = 8;

    // Paint objects
    private Paint matchBoxPaint;
    private Paint matchBoxWinnerPaint;
    private Paint matchBoxScheduledPaint;
    private Paint matchBoxLivePaint;
    private Paint teamTextPaint;
    private Paint scoreTextPaint;
    private Paint linePaint;
    private Paint strokePaint;

    // Data
    private List<TournamentMatch> matches;
    private Map<String, RectF> matchBoxes; // matchId -> RectF
    private OnMatchClickListener listener;
    
    // Layout dimensions
    private int numRounds;
    private int maxTeamsInRound;

    public interface OnMatchClickListener {
        void onMatchClick(TournamentMatch match);
    }

    public BracketView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public BracketView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BracketView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        matches = new ArrayList<>();
        matchBoxes = new HashMap<>();

        // Initialize paints
        matchBoxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        matchBoxPaint.setColor(ContextCompat.getColor(context, R.color.white));
        matchBoxPaint.setStyle(Paint.Style.FILL);

        matchBoxWinnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        matchBoxWinnerPaint.setColor(ContextCompat.getColor(context, R.color.success_light));
        matchBoxWinnerPaint.setStyle(Paint.Style.FILL);

        matchBoxScheduledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        matchBoxScheduledPaint.setColor(ContextCompat.getColor(context, R.color.scheduled_light));
        matchBoxScheduledPaint.setStyle(Paint.Style.FILL);

        matchBoxLivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        matchBoxLivePaint.setColor(ContextCompat.getColor(context, R.color.live_light));
        matchBoxLivePaint.setStyle(Paint.Style.FILL);

        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setColor(ContextCompat.getColor(context, R.color.md_theme_light_outline));
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2);

        teamTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        teamTextPaint.setColor(ContextCompat.getColor(context, R.color.md_theme_light_onSurface));
        teamTextPaint.setTextSize(TEXT_SIZE_TEAM * context.getResources().getDisplayMetrics().density);
        teamTextPaint.setTextAlign(Paint.Align.LEFT);

        scoreTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scoreTextPaint.setColor(ContextCompat.getColor(context, R.color.md_theme_light_onSurfaceVariant));
        scoreTextPaint.setTextSize(TEXT_SIZE_SCORE * context.getResources().getDisplayMetrics().density);
        scoreTextPaint.setTextAlign(Paint.Align.RIGHT);
        scoreTextPaint.setFakeBoldText(true);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(ContextCompat.getColor(context, R.color.md_theme_light_outline));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2);
    }

    public void setMatches(List<TournamentMatch> matches) {
        this.matches = matches != null ? matches : new ArrayList<>();
        calculateDimensions();
        invalidate();
        requestLayout();
    }

    public void setOnMatchClickListener(OnMatchClickListener listener) {
        this.listener = listener;
    }

    private void calculateDimensions() {
        if (matches.isEmpty()) {
            numRounds = 0;
            maxTeamsInRound = 0;
            return;
        }

        // Calculate number of rounds based on match count
        // For a knockout tournament: quarters (4), semis (2), final (1)
        int totalMatches = matches.size();
        if (totalMatches >= 7) {
            numRounds = 4; // Round of 16, quarters, semis, final
            maxTeamsInRound = 8;
        } else if (totalMatches >= 3) {
            numRounds = 3; // Quarters, semis, final
            maxTeamsInRound = 4;
        } else if (totalMatches >= 1) {
            numRounds = 2; // Semis, final
            maxTeamsInRound = 2;
        } else {
            numRounds = 1; // Just final
            maxTeamsInRound = 1;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (matches.isEmpty()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int width = (numRounds * MATCH_BOX_WIDTH) + ((numRounds + 1) * HORIZONTAL_SPACING) + getPaddingLeft() + getPaddingRight();
        int height = (maxTeamsInRound * MATCH_BOX_HEIGHT) + ((maxTeamsInRound + 1) * VERTICAL_SPACING) + getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (matches.isEmpty()) {
            return;
        }

        matchBoxes.clear();
        
        // Draw bracket from left to right
        drawBracket(canvas);
    }

    private void drawBracket(Canvas canvas) {
        // Group matches by round
        Map<Integer, List<TournamentMatch>> matchesByRound = groupMatchesByRound();

        int round = 0;
        for (Map.Entry<Integer, List<TournamentMatch>> entry : matchesByRound.entrySet()) {
            List<TournamentMatch> roundMatches = entry.getValue();
            int matchCount = roundMatches.size();
            
            // Calculate vertical spacing for this round
            int totalHeight = getHeight() - getPaddingTop() - getPaddingBottom();
            int availableHeight = totalHeight - (matchCount * MATCH_BOX_HEIGHT);
            int spacing = matchCount > 1 ? availableHeight / (matchCount + 1) : availableHeight / 2;

            for (int i = 0; i < roundMatches.size(); i++) {
                TournamentMatch match = roundMatches.get(i);
                
                // Calculate position
                float left = getPaddingLeft() + (round * (MATCH_BOX_WIDTH + HORIZONTAL_SPACING)) + HORIZONTAL_SPACING;
                float top = getPaddingTop() + spacing + (i * (MATCH_BOX_HEIGHT + spacing));
                float right = left + MATCH_BOX_WIDTH;
                float bottom = top + MATCH_BOX_HEIGHT;

                RectF matchBox = new RectF(left, top, right, bottom);
                // TODO: TournamentMatch needs getId() or use getMatchId()
                matchBoxes.put(match.getMatchId(), matchBox);

                // Draw match box
                drawMatchBox(canvas, matchBox, match);

                // Draw connecting lines to next round (if not final)
                if (round < numRounds - 1) {
                    drawConnectingLine(canvas, matchBox, round, i, matchCount);
                }
            }

            round++;
        }
    }

    private void drawMatchBox(Canvas canvas, RectF box, TournamentMatch match) {
        // TODO: TournamentMatch is a link table and doesn't have display fields
        // Need to load Match entity and team names to populate this view
        // For now, use scheduled background for all matches
        Paint bgPaint = matchBoxScheduledPaint;

        // Draw background
        canvas.drawRoundRect(box, CORNER_RADIUS, CORNER_RADIUS, bgPaint);
        canvas.drawRoundRect(box, CORNER_RADIUS, CORNER_RADIUS, strokePaint);

        // Draw team names and scores
        float padding = 8 * getResources().getDisplayMetrics().density;
        float textY = box.top + padding + (teamTextPaint.getTextSize() / 2);

        // Team 1 - TODO: Load from Match entity
        String team1Name = "Team A";
        canvas.drawText(truncateText(team1Name, MATCH_BOX_WIDTH - padding * 2), 
                       box.left + padding, textY, teamTextPaint);

        // Team 2
        textY += MATCH_BOX_HEIGHT / 2;
        String team2Name = "Team B";
        canvas.drawText(truncateText(team2Name, MATCH_BOX_WIDTH - padding * 2), 
                       box.left + padding, textY, teamTextPaint);

        // Draw separator line
        float lineY = box.top + (MATCH_BOX_HEIGHT / 2);
        canvas.drawLine(box.left + padding, lineY, box.right - padding, lineY, linePaint);
    }

    private void drawConnectingLine(Canvas canvas, RectF fromBox, int round, int matchIndex, int matchCount) {
        float fromX = fromBox.right;
        float fromY = fromBox.top + (MATCH_BOX_HEIGHT / 2);
        
        float toX = fromX + HORIZONTAL_SPACING;
        
        // Calculate the connection point for the next round
        // Pairs of matches connect to the same match in next round
        int nextMatchIndex = matchIndex / 2;
        float toY;
        
        if (matchIndex % 2 == 0) {
            // Top match of pair - line goes down
            toY = fromY + (MATCH_BOX_HEIGHT / 2) + (VERTICAL_SPACING / 2);
        } else {
            // Bottom match of pair - line goes up
            toY = fromY - (MATCH_BOX_HEIGHT / 2) - (VERTICAL_SPACING / 2);
        }

        // Draw horizontal line
        canvas.drawLine(fromX, fromY, fromX + (HORIZONTAL_SPACING / 2), fromY, linePaint);
        
        // Draw vertical line
        canvas.drawLine(fromX + (HORIZONTAL_SPACING / 2), fromY, 
                       fromX + (HORIZONTAL_SPACING / 2), toY, linePaint);
        
        // Draw horizontal line to next box
        canvas.drawLine(fromX + (HORIZONTAL_SPACING / 2), toY, toX, toY, linePaint);
    }

    private Map<Integer, List<TournamentMatch>> groupMatchesByRound() {
        Map<Integer, List<TournamentMatch>> grouped = new HashMap<>();
        
        for (TournamentMatch match : matches) {
            int round = getRoundNumber(match);
            if (!grouped.containsKey(round)) {
                grouped.put(round, new ArrayList<>());
            }
            grouped.get(round).add(match);
        }
        
        return grouped;
    }

    private int getRoundNumber(TournamentMatch match) {
        // TODO: TournamentMatch doesn't have getStage() - need to load TournamentStage entity
        // For now, return 0 as default
        return 0;
        
        /*
        // Determine round based on match stage or sequence
        String stage = match.getStage();
        if (stage == null) return 0;
        
        if (stage.contains("Final") && !stage.contains("Semi")) {
            return numRounds - 1; // Final is last round
        } else if (stage.contains("Semi")) {
            return numRounds - 2; // Semi-finals are second to last
        } else if (stage.contains("Quarter")) {
            return numRounds - 3; // Quarter-finals are third to last
        } else {
            return 0; // Earlier rounds
        }
        */
    }

    private String truncateText(String text, float maxWidth) {
        if (text == null) return "";
        
        Rect bounds = new Rect();
        teamTextPaint.getTextBounds(text, 0, text.length(), bounds);
        
        if (bounds.width() <= maxWidth) {
            return text;
        }
        
        // Truncate with ellipsis
        String truncated = text;
        while (bounds.width() > maxWidth - 20 && truncated.length() > 0) {
            truncated = truncated.substring(0, truncated.length() - 1);
            teamTextPaint.getTextBounds(truncated + "...", 0, truncated.length() + 3, bounds);
        }
        
        return truncated + "...";
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float y = event.getY();
            
            // Check if touch is within any match box
            for (Map.Entry<String, RectF> entry : matchBoxes.entrySet()) {
                RectF box = entry.getValue();
                if (box.contains(x, y)) {
                    // Find the match and trigger listener
                    String matchId = entry.getKey();
                    for (TournamentMatch match : matches) {
                        // TODO: Use getMatchId() since getId() doesn't exist on TournamentMatch
                        if (match.getMatchId().equals(matchId)) {
                            if (listener != null) {
                                listener.onMatchClick(match);
                            }
                            return true;
                        }
                    }
                }
            }
        }
        
        return super.onTouchEvent(event);
    }
}
