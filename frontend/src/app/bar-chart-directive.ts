import { Directive, ElementRef, OnInit, OnDestroy, Input, OnChanges, SimpleChanges } from '@angular/core';
import Chart from 'chart.js/auto';

@Directive({
  selector: '[appBarChart]'
})
export class BarChartDirective implements OnInit, OnDestroy, OnChanges {
  private chart: any;

  @Input() labels: string[] = ['Open', 'Closed', 'Archived'];
  @Input() chartData: number[] = [0, 0, 0];
  @Input() chartLabel: string = 'Cases by Status';

  // Legacy compatibility inputs
  @Input() documentiValidi?: number;
  @Input() documentiInScadenza?: number;
  @Input() documentiScaduti?: number;

  constructor(private el: ElementRef) {}

  ngOnInit(): void {
    this.createBarChart();
    this.updateChartSize();
    window.addEventListener('resize', this.updateChartSize.bind(this));
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.chart) {
      this.updateChartData();
    }
  }

  ngOnDestroy(): void {
    window.removeEventListener('resize', this.updateChartSize.bind(this));
    if (this.chart) {
      this.chart.destroy();
    }
  }

  private getData(): { labels: string[], data: number[], colors: string[] } {
    if (this.documentiValidi !== undefined) {
      return {
        labels: ['Active Held', 'Nearing Retention', 'Disposal Eligible'],
        data: [this.documentiValidi || 0, this.documentiInScadenza || 0, this.documentiScaduti || 0],
        colors: ['#198754', '#ffc107', '#dc3545']
      };
    }
    return {
      labels: this.labels,
      data: this.chartData,
      colors: ['#0d6efd', '#6c757d', '#198754']
    };
  }

  private createBarChart(): void {
    const canvas = this.el.nativeElement;
    const ctx = canvas.getContext('2d');
    const chartInfo = this.getData();

    this.chart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: chartInfo.labels,
        datasets: [{
          label: this.chartLabel,
          data: chartInfo.data,
          backgroundColor: chartInfo.colors,
          borderWidth: 1,
          borderRadius: 6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              stepSize: 1
            }
          }
        },
        plugins: {
          legend: {
            display: false
          }
        }
      }
    });
  }

  private updateChartData(): void {
    if (!this.chart) return;
    const chartInfo = this.getData();
    this.chart.data.labels = chartInfo.labels;
    this.chart.data.datasets[0].data = chartInfo.data;
    this.chart.data.datasets[0].backgroundColor = chartInfo.colors;
    this.chart.update();
  }

  private updateChartSize(): void {
    const canvas = this.el.nativeElement;
    const parent = canvas.parentElement;
    if (parent) {
      canvas.width = parent.offsetWidth;
      canvas.height = parent.offsetHeight;
      if (this.chart) {
        this.chart.resize();
      }
    }
  }
}
