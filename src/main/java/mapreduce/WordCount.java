package mapreduce;

import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class WordCount {
	//继承mapper接口，设置map的输入类型为<LongWritable,Text>
	//输出类型为<Text,IntWritable>
	public static class WordCountMap extends Mapper<LongWritable, Text, Text, IntWritable> {
		//one表示单词出现一次
		private final IntWritable one = new IntWritable(1);
		//word存储切下的单词
		private Text word = new Text();

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			//对输入的行切词
			String line = value.toString();
			StringTokenizer token = new StringTokenizer(line);
			while (token.hasMoreTokens()) {
				//切下的单词存入word
				word.set(token.nextToken());
				context.write(word, one);
			}
		}
	}

	//继承reducer接口，设置reduce的输入类型<Text,IntWritable>
	//输出类型为<Text,IntWritable>
	public static class WordCountReduce extends Reducer<Text, IntWritable, Text, IntWritable> {
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			int sum = 0;
			//对获取的<key,value-list>计算value的和
			for (IntWritable val : values) {
				//将频数设置到result
				sum += val.get();
			}
			//收集结果
			context.write(key, new IntWritable(sum));
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = new Job(conf);
		job.setJarByClass(WordCount.class);
		job.setJobName("wordcount");
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		job.setMapperClass(WordCountMap.class);
		job.setReducerClass(WordCountReduce.class);
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.waitForCompletion(true);
	}
}
