package com.dubblej.simon

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.media.SoundPool
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*

private const val DIALOG_OPTIONS = "DialogOptions"
private const val DIALOG_DIFFICULTY = "DialogDifficulty"
private const val DIALOG_START_NEW_GAME = "DialogStartNewGame"
private const val DIALOG_END_OF_GAME = "DialogEndOfGame"
private const val KEY_PATTERN_INDEX = "PatternIndex"

class MainActivity : AppCompatActivity(), StartNewGameFragment.OnInputListener, EndGameFragment.OnInputListener, OptionsFragment.OnInputListener, CoroutineScope by MainScope(){

    private val model: Game by viewModels()
    private val frames = arrayOfNulls<FrameLayout>(4)
    private lateinit var soundPool: SoundPool
    private var blueSound = 0
    private var yellowSound = 0
    private var redSound = 0
    private var greenSound = 0
    private var gameOverSoundId = 0
    private var countDownSound = 0
    private var finalCountDownSound = 0
    private val pads = mutableListOf<Pad>()
    private var acceptingUserInput = false
    private var speed = 500
    private var patternIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val padRecyclerView = findViewById<RecyclerView>(R.id.pads_recycler_view)
        padRecyclerView.adapter = PadAdapter()
        padRecyclerView.layoutManager = GridLayoutManager(this, 2)

        val scoreTextView = findViewById<TextView>(R.id.score_textView)

        val gameStateTextView = findViewById<TextView>(R.id.game_state_text_view)

        val fab = findViewById<FloatingActionButton>(R.id.fab)

        fab.setOnClickListener {
            addFragment(OptionsFragment(), DIALOG_OPTIONS)
        }

        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .build()

        blueSound = soundPool.load(this, R.raw.e_note, 1)
        yellowSound = soundPool.load(this, R.raw.c_sharp_note, 1)
        redSound = soundPool.load(this, R.raw.a_note, 1)
        greenSound = soundPool.load(this, R.raw.a_low_note, 1)

        gameOverSoundId = soundPool.load(this, R.raw.game_over, 1)
        countDownSound = soundPool.load(this, R.raw.countdown_sound, 1)
        finalCountDownSound = soundPool.load(this, R.raw.final_countdown_sound, 1)

        pads.add(Pad(0, R.color.green_off, R.color.green_on, greenSound))
        pads.add(Pad(1, R.color.red_off, R.color.red_on, redSound))
        pads.add(Pad(2, R.color.yellow_off, R.color.yellow_on, yellowSound))
        pads.add(Pad(3, R.color.blue_off, R.color.blue_on, blueSound))

        val timerObserver = Observer<Int> { secondsLeft ->

            gameStateTextView.text = secondsLeft.toString()

            if (secondsLeft != 0)
                soundPool.play(countDownSound, .5f, .5f, 0, 0, 1f)

            else   {
                soundPool.play(finalCountDownSound, .5f, .5f, 0, 0, 1f)
            }

        }
        model.secondsTilGameStart.observe(this, timerObserver)

        val gameStartedObserver = Observer<Boolean> {gameStarted ->
            if (gameStarted) {

                showPattern(0)

                model.setGameStarted()

            }
        }
        model.gameStarted.observe(this, gameStartedObserver)

        val scoreObserver = Observer<Int> { newScore ->
            scoreTextView.text = "Score: "+ newScore.toString()
        }
        model.userScore.observe(this, scoreObserver)


        val gameOverObserver = Observer<Boolean> {isGameOver ->
            if (isGameOver) {

                soundPool.play(gameOverSoundId, .25f, .25f, 0, 0, 1f)

                addFragment(EndGameFragment(), DIALOG_END_OF_GAME)

            }

            if(!isGameOver) {
                removeFragment(DIALOG_END_OF_GAME)
                removeFragment(DIALOG_START_NEW_GAME)
                removeFragment(DIALOG_OPTIONS)
                removeFragment(DIALOG_DIFFICULTY)
            }
        }
        model.gameOver.observe(this, gameOverObserver)

        val patternCompleteObserver = Observer<Boolean> {isPatternComplete ->
            if (isPatternComplete && model.gameOver.value == false) {
                showPattern(2000)
            }

        }
        model.patternComplete.observe(this, patternCompleteObserver)

        val acceptingUserInputObserver = Observer<Boolean> {isAcceptingUserInput ->
            acceptingUserInput = isAcceptingUserInput

            if (acceptingUserInput) {
                gameStateTextView.text = "Play"
                patternIndex = 0
            }

            else
                gameStateTextView.text = "Watch"
        }
        model.acceptingUserInput.observe(this, acceptingUserInputObserver)

        val difficultyObserver = Observer<Int> { difficultyLevel ->
                when(difficultyLevel) {
                    0 -> speed = 166
                    1 -> speed = 333
                    2 -> speed = 500
                }
        }
        model.difficulty.observe(this, difficultyObserver)

        if (savedInstanceState != null) {
            if (model.acceptingUserInput.value == false && model.secondsTilGameStart.value == 0) {

                patternIndex = savedInstanceState.getInt(KEY_PATTERN_INDEX)

                resumePattern()
            }
        }

    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (model.acceptingUserInput.value == false)
            outState.putInt(KEY_PATTERN_INDEX, patternIndex)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
        soundPool.release()
    }

    override fun sendInput(userResponse: Int) {
        if (userResponse == Activity.RESULT_OK) {
            model.startNewGame()
        }

        else
            finish()
    }

    override fun buttonPressed(userResponse: Int) {
        removeFragment(DIALOG_OPTIONS)

        if (userResponse == Activity.RESULT_OK)
            addFragment(DifficultyFragment(), DIALOG_DIFFICULTY)

        else
            addFragment(StartNewGameFragment(), DIALOG_START_NEW_GAME)
    }

    private suspend fun lightUpPad(pad: Pad, firstDelay: Long, secondDelay: Long) {
        delay(firstDelay)
        frames[pad.id]?.backgroundTintList =
            AppCompatResources.getColorStateList(applicationContext, pad.colorOn)
        soundPool.play(pad.soundId, 1f, 1f, 0, 0, 1f)
        delay(secondDelay)
        frames[pad.id]?.backgroundTintList =
            AppCompatResources.getColorStateList(applicationContext, pad.colorOff)
    }

    private fun showPattern(delayTime: Long) = launch {
        delay(delayTime)
        model.pattern.forEachIndexed { index, i ->
            patternIndex = index
            lightUpPad(pads[i], 100, speed.toLong())
            model.isPatternCompleteShowing(index)
        }
    }

    private fun resumePattern() = launch {
        delay(500)
        for (i in patternIndex until model.pattern.size) {
            patternIndex = i
            lightUpPad(pads[model.pattern[i]], 100, speed.toLong())
            model.isPatternCompleteShowing(i)
        }
    }

    private fun addFragment(fragment: Fragment, fragmentString: String) {
        if (supportFragmentManager.findFragmentByTag(fragmentString) == null) {
            supportFragmentManager.beginTransaction()
                .add(fragment, fragmentString)
                .commit()
            supportFragmentManager.executePendingTransactions()
            Log.e("TAG", "Show! " + (supportFragmentManager
                .findFragmentByTag(fragmentString) == null))
        }
    }

    private fun removeFragment(fragmentString: String) {
        if (supportFragmentManager.findFragmentByTag(fragmentString) != null) {
            supportFragmentManager.findFragmentByTag(fragmentString)?.let {
                supportFragmentManager.beginTransaction()
                    .remove(it)
                    .commit()
            }
            supportFragmentManager.executePendingTransactions()
        }
    }

    private inner class PadHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val frameLayout : FrameLayout = itemView.findViewById(R.id.frame_layout)
        private lateinit var pad: Pad

        override fun onClick(v: View?) {

            if (acceptingUserInput) {

                launch {
                    lightUpPad(pad, 0, 500)
                }

                model.checkUserInput(pad.id)

            }

        }

        fun bindPad(pad: Pad) {
            itemView.setOnClickListener(this)
            frames[pad.id] = frameLayout
            frameLayout.backgroundTintList = AppCompatResources.getColorStateList(applicationContext, pad.colorOff)
            this.pad = pad
        }

    }

    private inner class PadAdapter() : RecyclerView.Adapter<PadHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PadHolder {
            val v: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.pad, parent, false)

            return PadHolder(v)
        }

        override fun onBindViewHolder(holder: PadHolder, position: Int) {
            holder.bindPad(pads[position])
        }

        override fun getItemCount(): Int {
            return pads.size
        }
    }
}